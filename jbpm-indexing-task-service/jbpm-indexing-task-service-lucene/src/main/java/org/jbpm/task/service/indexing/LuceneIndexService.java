package org.jbpm.task.service.indexing;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;

import com.thoughtworks.xstream.XStream;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.CompressionTools;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TrackingIndexWriter;
import org.apache.lucene.search.ControlledRealTimeReopenThread;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.jbpm.services.task.utils.ContentMarshallerHelper;
import org.jbpm.task.indexing.api.Filter;
import org.jbpm.task.indexing.api.QueryResult;
import org.jbpm.task.indexing.service.ExternalIndexService;
import org.jbpm.task.indexing.service.TaskContentReader;
import org.kie.api.runtime.Environment;
import org.kie.api.task.model.Attachment;
import org.kie.api.task.model.Comment;
import org.kie.api.task.model.Content;
import org.kie.api.task.model.I18NText;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.PeopleAssignments;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskData;

public class LuceneIndexService implements ExternalIndexService <Task> {

    private static final String STR = "_STR";
    private static final String TASK_BINARY = "task_binary";
    private static final String FAULT_CONTENT ="faultContent_binary";
    private static final String DOCUMENT_CONTENT = "documentContent_binary";
    private static final String OUTPUT_CONTENT = "output_binary";

    private Analyzer keywordAnalyzer = new KeywordAnalyzer();
    private Environment environment;

    private IndexWriter iw;
    private LuceneQueryBuilder queryBuilder;
    private XStream xs = new XStream();

    private TrackingIndexWriter tiw;
    private SearcherManager sm;
    private ControlledRealTimeReopenThread<IndexSearcher> reopener;

    private ThreadLocal<List<Document>> adds = new ThreadLocal<List<Document>>(){
        @Override
        protected List<Document> initialValue() {
            return new ArrayList<Document>();
        }
    };

    private ThreadLocal<List<Document>> updates = new ThreadLocal<List<Document>>(){
        @Override
        protected List<Document> initialValue() {
            return new ArrayList<Document>();
        }
    };


    public void LuceneIndexService(Environment environment) throws IOException {
        this.environment = environment;
        Directory directory = new RAMDirectory();
        queryBuilder = new LuceneQueryBuilder();

        iw = new IndexWriter(directory,
            new IndexWriterConfig(Version.LUCENE_47, keywordAnalyzer));

        sm = new SearcherManager(iw, true, new WarmSearchFactory());
        tiw = new TrackingIndexWriter(iw);
        reopener = new ControlledRealTimeReopenThread(tiw, sm,3,0);
        reopener.setDaemon(true);
        reopener.start();

    }

    private static class WarmSearchFactory extends SearcherFactory {
        public IndexSearcher newSearcher(IndexReader reader)
            throws IOException {
            IndexSearcher searcher = new IndexSearcher(reader);
            searcher.setSimilarity(new IsolationSimilarity());
            return new IndexSearcher(reader);
        }
    }

    private static class IsolationSimilarity extends DefaultSimilarity {
        public IsolationSimilarity() {
        }

        public float idf(int docFreq, int numDocs) {
            return (float) 1.0;
        }

        public float coord(int overlap, int maxOverlap) {
            return 1.0f;
        }

        public float lengthNorm(String fieldName, int numTerms) {
            return 1.0f;
        }
    }

    @Override
    public void prepare(Collection<Task> updates, Collection<Task> inserts,
        TaskContentReader reader) throws IOException {
        this.adds.get().clear();
        this.updates.get().clear();

        for (Task t : inserts) {
            tiw.addDocument(prepareDocument(t,reader));
        }
        for (Task t : updates) {
            tiw.updateDocument(new Term("id", String.valueOf(t.getId())),
                prepareDocument(t,reader));
        }
        tiw.getIndexWriter().prepareCommit();
    }

    private Document prepareDocument(Task t, TaskContentReader reader) {

        Document d = new Document();
        addKeyWordField("ALL", "ALL", d, false);
        d.add(new StoredField(TASK_BINARY, CompressionTools
            .compress(xs.toXML(t).getBytes(Charset.forName("UTF-8")))));
        //;
        for (I18NText text : t.getDescriptions()) {
            addDefaultField("description", text.getText(), d, true);
        }
        addKeyWordField("id", String.valueOf(t.getId()), d, false);
        for (I18NText name : t.getNames()) {
            addKeyWordField("name", name.getText(), d, true);
        }
        for (OrganizationalEntity admin : t.getPeopleAssignments()
            .getBusinessAdministrators()) {
            addKeyWordField("businessAdministrator", admin.getId(), d,
                true);
        }
        for (OrganizationalEntity potUser : t.getPeopleAssignments()
            .getPotentialOwners()) {
            addKeyWordField("potentialOwner", potUser.getId(), d, true);
        }
        addKeyWordField("taskInitiator",
            t.getPeopleAssignments().getTaskInitiator().getId(), d, true);
        addIntField("priority", t.getPriority(), d, false);
        for (I18NText subject : t.getSubjects()) {
            addKeyWordField("subject", subject.getText(), d, true);
        }

        prepareTaskDate(t.getId(), t.getTaskData(), d, reader);
        addKeyWordField("taskType", t.getTaskType(), d, false);
        addKeyWordField("type", "Task", d, false);
        return d;
    }


    private void prepareTaskDate(long taskId, TaskData data, Document d, TaskContentReader reader) {
        addDateField("activationTime", data.getActivationTime(), d, true);
        addKeyWordField("actualOwner", data.getActualOwner().getId(), d, true);

        for (Attachment att : data.getAttachments()) {
            //TODO we could add in separate doc and use BlockJoinQuery
            addDateField("attachment_attachedAt", att.getAttachedAt(), d, false);
            addKeyWordField("attachment_attachedBy",
                att.getAttachedBy().getId(), d, false);
            addLongField("attachment_attachmentContentId",
                att.getAttachmentContentId(), d, false);
            addKeyWordField("attachment_contentType", att.getContentType(), d,
                false);
            addLongField("attachment_Id", att.getId(), d, false);
            addKeyWordField("attachment_name", att.getName(), d, false);
            addIntField("attachment_size", att.getSize(), d, false);
        }


        for (Comment c : data.getComments()) {
            addDefaultField("comment", c.getText(),d, true);
            addDateField("comment_date", c.getAddedAt(), d, false);
            addKeyWordField("comment_addedBy",c.getAddedBy().getId(), d, false);
            addLongField("comment_id", c.getId(), d, false);
            c.getText();c.getAddedAt();c.getAddedBy();
        }

        addKeyWordField("createdBy", data.getCreatedBy().getId(), d, true);
        addDateField("createdOn",data.getCreatedOn(),d,true);

        //load content?
        addContent(taskId, data.getDocumentContentId(), "documentContent", d, data.getDocumentType(), reader);

        addLongField("deploymentId", data.getDocumentContentId(), d, false);
        addKeyWordField("documentType", data.getDocumentType(), d, true);

        addDateField("expirationTime", data.getExpirationTime(), d, true);

        addContent(taskId, data.getFaultContentId(), "FaultContent", d, data.getFaultType(), reader);
        addKeyWordField("faultName", data.getFaultName(), d, false);

        addContent(taskId, data.getOutputContentId(), "outputContent", d, data.getOutputType(), reader);

        addLongField("parentId", data.getParentId(), d, false);
        addKeyWordField("previousStatus", data.getPreviousStatus().name(), d,
            false);
        addKeyWordField("processId", data.getProcessId(), d, false);
        addLongField("processInstanceId", data.getProcessInstanceId(), d, false);
        addIntField("processSessionId", data.getProcessSessionId(), d, false);
        addKeyWordField("status", data.getStatus().name(), d, false);
    }

    private void addContent(long taskId, long id, String prefix, Document doc, String type, TaskContentReader reader) {
        Content data = reader.getTaskContent(taskId, id);
        try {

            Class c = Thread.currentThread().getContextClassLoader().loadClass(type);
            if (!(c == Map.class || c.isAssignableFrom(Map.class))) {
                throw new IllegalArgumentException("Only map content is supported");

            }
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("type not loadable", e);
        }
        Map<String, Object> content =
            (Map<String, Object>) ContentMarshallerHelper
                .unmarshall(data.getContent(), environment);
        doc.add(new StoredField(prefix + "_binary", CompressionTools
            .compress(xs.toXML(content).getBytes(Charset.forName("UTF-8")))));
        for (Map.Entry<String, Object> en : content.entrySet()) {
            if (en.getValue().getClass() == Date.class) {
                addDateField(prefix + "_" + en.getKey(), (Date) en.getValue(), doc, false);
            } else if (en.getValue().getClass() == Long.class) {
                addLongField(prefix + "_" + en.getKey(), (Long) en.getValue(),
                    doc, false);
            } else if (en.getValue().getClass() == Integer.class) {
                addIntField(prefix + "_" + en.getKey(), (Integer) en.getValue(),
                    doc, false);
            } else {
                addKeyWordField(prefix + "_" + en.getKey(),
                    en.getValue().toString(),
                    doc, false);
            }
        }
    }

    @Override
    public void commit() throws IOException {
        tiw.getIndexWriter().commit();
    }

    @Override
    public void rollback() {
        try {
        tiw.getIndexWriter().rollback();
        } catch (IOException e) {
           throw new RuntimeException("Unable to rollback",e);
        }
    }

    @Override
    public void syncIndex(Iterator<Task> previousTasks, TaskContentReader reader) throws IOException {
        while (previousTasks.hasNext()) {
            iw.addDocument(prepareDocument(previousTasks.next(), reader));
        }
    }



    @Override
    public QueryResult<Task> find( int offset, int count,
        Comparator<Task> comparator, Filter<?, ?>... filters)  throws IOException{
        IndexSearcher search = getSearcher(tiw.getGeneration());

        Query query = queryBuilder.buildQuery(search, filters);

        //Sort sort = indexUtil.getSort(comparator);


        TopDocs td = search.search(query, offset + count);
           //: search.search(query, offset + count, sort);
        int c = 0;
        List<Task> l = new ArrayList();
        try {
            while (c < count && offset + c < td.totalHits) {
                Document doc = search.doc(td.scoreDocs[offset + c++].doc);
                IndexedTask in = new IndexedTask();
                in.embedded = (Task) xs.fromXML(new ByteArrayInputStream(CompressionTools.
                    decompress(doc.getBinaryValue(TASK_BINARY))));
                in.faultContent = (Map<String,Object>)
                    xs.fromXML(new ByteArrayInputStream(
                    CompressionTools.decompress(
                        doc.getBinaryValue(FAULT_CONTENT))));
                in.documentContent = (Map<String,Object>)
                    xs.fromXML(new ByteArrayInputStream(
                        CompressionTools.decompress(doc.getBinaryValue(DOCUMENT_CONTENT))));
                in.outputContent = (Map<String,Object>)
                    xs.fromXML(new ByteArrayInputStream(
                        CompressionTools.decompress(doc.getBinaryValue(OUTPUT_CONTENT))));
                l.add(in);
            }
        } catch (DataFormatException e) {
            e.printStackTrace();
        } finally {
            sm.release(search);
        }
        return new QueryResult(offset, td.totalHits, l);
    }

    private IndexSearcher getSearcher(long neededCommitPoint)
        throws IOException {
        try {
            reopener.waitForGeneration(neededCommitPoint);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Interrupted while waiting for generation");
        }
        return sm.acquire();
    }




    private void addDefaultField(String name, String value, Document d,
        boolean includeInFreeText) {
        if (value == null || "".equals(value)) {
            return;
        }
        d.add(new TextField(name, value, Field.Store.NO));
        if (includeInFreeText) {
            addFreeTextField(value, d);
        }
    }

    private void addFreeTextField(String s, Document doc) {
        if (s != null && s.length() > 0) {
            doc.add(new TextField("freetext", s, Field.Store.NO));
        }
    }

    private void addKeyWordField(String name, String value, Document d,
        boolean includeInFreeText) {
        if (value == null || "".equals(value)) {
            return;
        }
        d.add(new StringField(name, value, Field.Store.NO));
        if (includeInFreeText) {
            addFreeTextField(value, d);
        }
    }

    private void addDateField(String name, Date value, Document d,
        boolean includeInFreeText) {
        if (value == null) {
            return;
        }
        String strVal = DateTools.dateToString(value, DateTools.Resolution.DAY);
        LongField field = new LongField(name, value.getTime(), Field.Store.NO);
        StringField sf = new StringField(name + STR, strVal, Field.Store.NO);
        d.add(field);
        d.add(sf);
        if (includeInFreeText) {
            addFreeTextField(strVal, d);
        }
    }

    private void addIntField(String name, int value, Document d,
        boolean includeInFreeText) {
        String strVal = String.valueOf(value);
        IntField field = new IntField(name, value, Field.Store.NO);
        StringField sf = new StringField(name + STR, strVal, Field.Store.NO);
        d.add(field);
        d.add(sf);
        if (includeInFreeText) {
            addFreeTextField(strVal, d);
        }
    }

    private void addLongField(String name, Long value, Document doc,
        boolean includeInFreeText) {
        if (value == null) {
            return;
        }
        String strVal = value.toString();
        LongField field = new LongField(name, value, Field.Store.NO);
        StringField sf = new StringField(name + STR, strVal, Field.Store.NO);
        doc.add(field);
        doc.add(sf);
        if (includeInFreeText) {
            addFreeTextField(strVal, doc);
        }
    } 
    
    private static class IndexedTask implements Task {


        private Task embedded;
        private Map<String, Object> faultContent;
        private Map<String, Object> outputContent;
        private Map<String, Object> documentContent;

        public  IndexedTask(){}

        private Map<String, Object> getFaultContent(){
            return faultContent;
        }

        private Map<String, Object> getOutputContent() {
            return outputContent;
        }

        private Map<String, Object> getDocumentContent(){
            return documentContent;
        }

        @Override
        public Long getId() {
            return embedded.getId();
        }

        @Override
        public int getPriority() {
            return embedded.getPriority();
        }

        @Override
        public List<I18NText> getNames() {
            return embedded.getNames();
        }

        @Override
        public List<I18NText> getSubjects() {
            return embedded.getSubjects();
        }

        @Override
        public List<I18NText> getDescriptions() {
            return embedded.getSubjects();
        }

        @Override
        public PeopleAssignments getPeopleAssignments() {
            return embedded.getPeopleAssignments();
        }

        @Override
        public TaskData getTaskData() {
            return embedded.getTaskData();
        }

        @Override
        public String getTaskType() {
            return embedded.getTaskType();
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
             embedded.writeExternal(out);
        }

        @Override
        public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
            embedded.readExternal(in);
        }
    }
}
