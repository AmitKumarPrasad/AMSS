package com.edusphere.documents;

import com.edusphere.operations.AuditService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.*; import java.nio.file.Paths; import java.security.*; import java.util.*;

@Service
public class DocumentService {
    private static final Set<String> AUDIENCES=Set.of("SUPER_ADMIN","SCHOOL_ADMIN","TEACHER","ACCOUNTANT","PARENT","STUDENT");
    private static final int MAX_LIST=100;
    private final DocumentRepository repository; private final AuditService auditService; private final DocumentStorage storage;
    public DocumentService(DocumentRepository repository,AuditService auditService,DocumentStorage storage){this.repository=repository;this.auditService=auditService;this.storage=storage;}

    @Transactional public DocumentView register(UUID schoolId,CreateDocumentRequest request,UUID uploadedBy){
        if(schoolId==null||uploadedBy==null||request==null)bad("schoolId, uploadedBy and document request are required");
        String title=requiredText(request.title(),"title"), fileName=requiredText(request.fileName(),"fileName"), contentType=requiredText(request.contentType(),"contentType"), key=requiredText(request.storageKey(),"storageKey");
        if(repository.existsBySchoolIdAndStorageKey(schoolId,key))throw new ResponseStatusException(HttpStatus.CONFLICT,"Document storage key already exists");
        Document saved=repository.save(new Document(schoolId,title,trimNullable(request.description()),sanitizeFileName(fileName),contentType,key,trimNullable(request.checksum()),normalizeAudience(request.audienceRole()),uploadedBy));
        auditService.record(schoolId,uploadedBy,"DOCUMENT_REGISTERED","DOCUMENT",saved.getId(),"{\"title\":\""+jsonEscape(saved.getTitle())+"\",\"storageKey\":\""+jsonEscape(saved.getStorageKey())+"\"}");
        return toView(saved);
    }
    @Transactional public DocumentView upload(UUID schoolId,MultipartFile file,String title,String description,String audienceRole,UUID uploadedBy){
        if(schoolId==null||uploadedBy==null)bad("schoolId and uploadedBy are required"); if(file==null||file.isEmpty())bad("file must not be empty");
        String fileName=sanitizeFileName(file.getOriginalFilename()), contentType=file.getContentType()==null||file.getContentType().isBlank()?"application/octet-stream":file.getContentType().trim(), key=schoolId+"/"+UUID.randomUUID()+"/"+fileName;
        try{storage.store(key,file.getInputStream(),file.getSize(),contentType);String checksum=sha256(storage.open(key));return register(schoolId,new CreateDocumentRequest(title,description,fileName,contentType,key,checksum,audienceRole),uploadedBy);}
        catch(IOException e){try{storage.delete(key);}catch(IOException ignored){} throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"document storage failed",e);}
    }
    @Transactional(readOnly=true) public DocumentDownload download(UUID schoolId,UUID documentId,Collection<String> roles){
        Document document=get(schoolId,documentId); if(!"PUBLISHED".equals(document.getStatus())||!isVisible(document.getAudienceRole(),normalizeRoles(roles)))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Document not found");
        try{return new DocumentDownload(storage.open(document.getStorageKey()),document.getFileName(),document.getContentType(),storage.size(document.getStorageKey()));}catch(IOException e){throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Document content not found",e);}
    }
    @Transactional(readOnly=true) public List<DocumentView> list(UUID schoolId,boolean publishedOnly,Collection<String> roles,int limit){
        if(limit<1||limit>MAX_LIST)bad("limit must be between 1 and "+MAX_LIST);
        var pageable=PageRequest.of(0,limit,Sort.by(Sort.Direction.DESC,"createdAt"));
        var page=publishedOnly?repository.findBySchoolIdAndStatusOrderByCreatedAtDesc(schoolId,"PUBLISHED",pageable):repository.findBySchoolIdOrderByCreatedAtDesc(schoolId,pageable);
        Set<String> allowed=normalizeRoles(roles); return page.stream().filter(d->!publishedOnly||isVisible(d.getAudienceRole(),allowed)).map(this::toView).toList();
    }
    @Transactional public DocumentView publish(UUID schoolId,UUID documentId){Document d=get(schoolId,documentId);if(!"DRAFT".equals(d.getStatus()))bad("Only draft documents can be published");d.publish();return toView(repository.save(d));}
    @Transactional public DocumentView archive(UUID schoolId,UUID documentId){Document d=get(schoolId,documentId);if(!"PUBLISHED".equals(d.getStatus()))bad("Only published documents can be archived");d.archive();return toView(repository.save(d));}
    private Document get(UUID schoolId,UUID id){return repository.findById(id).filter(d->schoolId.equals(d.getSchoolId())).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Document not found"));}
    private String normalizeAudience(String v){String a=DocumentAudience.normalize(v);if("PUBLIC".equals(a))return null;if(!AUDIENCES.contains(a))bad("invalid audienceRole");return a;}
    private Set<String> normalizeRoles(Collection<String> roles){if(roles==null)return Set.of();return roles.stream().filter(Objects::nonNull).map(v->v.replace("ROLE_","").trim().toUpperCase(Locale.ROOT)).filter(AUDIENCES::contains).collect(java.util.stream.Collectors.toUnmodifiableSet());}
    private boolean isVisible(String audience,Set<String> roles){return audience==null||audience.isBlank()||roles.contains("SUPER_ADMIN")||roles.contains(audience);}
    private String sanitizeFileName(String v){String n=v==null?"document":Paths.get(v).getFileName().toString().trim();return n.isBlank()||".".equals(n)||"..".equals(n)?"document":n.replaceAll("[^a-zA-Z0-9._-]","_");}
    private String sha256(InputStream in)throws IOException{try{MessageDigest md=MessageDigest.getInstance("SHA-256");in.transferTo(new OutputStream(){public void write(int b){md.update((byte)b);}public void write(byte[] b,int o,int l){md.update(b,o,l);}});return HexFormat.of().formatHex(md.digest());}catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}finally{in.close();}}
    private String trimNullable(String v){return v==null||v.isBlank()?null:v.trim();} private String requiredText(String v,String f){if(v==null||v.isBlank())bad(f+" is required");return v.trim();} private String jsonEscape(String v){return v.replace("\","\\").replace(""","\"");} private void bad(String m){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,m);}
    private DocumentView toView(Document d){return new DocumentView(d.getId(),d.getSchoolId(),d.getTitle(),d.getDescription(),d.getFileName(),d.getContentType(),d.getStorageKey(),d.getChecksum(),d.getAudienceRole(),d.getStatus(),d.getUploadedBy(),d.getCreatedAt(),d.getUpdatedAt());}
    public record CreateDocumentRequest(String title,String description,String fileName,String contentType,String storageKey,String checksum,String audienceRole){}
    public record DocumentView(UUID id,UUID schoolId,String title,String description,String fileName,String contentType,String storageKey,String checksum,String audienceRole,String status,UUID uploadedBy,java.time.Instant createdAt,java.time.Instant updatedAt){}
    public record DocumentDownload(InputStream content,String fileName,String contentType,long size){}
}