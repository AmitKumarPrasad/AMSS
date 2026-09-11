package com.edusphere.notifications;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
@Service
public class NotificationService {
 private static final List<String> CHANNELS=List.of("IN_APP","EMAIL","SMS");
 private final NotificationRepository repository;
 public NotificationService(NotificationRepository repository){this.repository=repository;}
 @Transactional public NotificationView enqueue(UUID schoolId,UUID recipientUserId,String channel,String subject,String body){
  String c=channel==null?"IN_APP":channel.trim().toUpperCase(Locale.ROOT); if(!CHANNELS.contains(c)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid notification channel");
  if(body==null||body.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"body is required");
  return view(repository.save(new Notification(schoolId,recipientUserId,c,subject==null?null:subject.trim(),body.trim())));
 }
 @Transactional(readOnly=true) public List<NotificationView> list(UUID schoolId,UUID userId){return repository.findBySchoolIdAndRecipientUserIdOrderByCreatedAtDesc(schoolId,userId).stream().map(this::view).toList();}
 @Transactional public int markReadyFailures(){return 0;}
 private NotificationView view(Notification n){return new NotificationView(n.getId(),n.getSchoolId(),n.getRecipientUserId(),n.getChannel(),n.getSubject(),n.getBody(),n.getStatus(),n.getAttempts(),n.getAvailableAt(),n.getLastError(),n.getSentAt(),n.getCreatedAt());}
 public record NotificationView(UUID id,UUID schoolId,UUID recipientUserId,String channel,String subject,String body,String status,int attempts,Instant availableAt,String lastError,Instant sentAt,Instant createdAt){}
}
