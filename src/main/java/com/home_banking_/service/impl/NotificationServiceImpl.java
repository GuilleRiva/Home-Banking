package com.home_banking_.service.impl;

import com.home_banking_.dto.request.NotificationCommand;
import com.home_banking_.dto.response.NotificationResponseDto;
import com.home_banking_.enums.NotificationReferenceType;
import com.home_banking_.exceptions.custom.BusinessException;
import com.home_banking_.exceptions.custom.ResourceNotFoundException;
import com.home_banking_.mappers.NotificationMapper;
import com.home_banking_.model.Notification;
import com.home_banking_.repository.NotificationRepository;
import com.home_banking_.service.NotificationService;
import com.home_banking_.service.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final CurrentUserService currentUserService;

    private static final int MAX_TITLE_LENGTH = 120;
    private static final int MAX_MESSAGE_LENGTH = 500;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void notifyUser(NotificationCommand command) {
        validateNotificationCommand(command);

        Notification notification = buildNotification(command);

        notificationRepository.save(notification);

    }


    @Override
    public List<NotificationResponseDto> getMyNotifications() {
        String email = currentUserService.getCurrentUserEmail();

        return notificationRepository
                .findByRecipientEmailOrderByCreatedAtDesc(email)
                .stream()
                .map(notificationMapper::toResponseDto)
                .toList();
    }

    @Override
    public List<NotificationResponseDto> getMyUnreadNotifications() {
        String email = currentUserService.getCurrentUserEmail();

        return notificationRepository
                .findByRecipientEmailAndReadFalseOrderByCreatedAtDesc(email)
                .stream()
                .map(notificationMapper::toResponseDto)
                .toList();
    }

    @Override
    public long countMyUnreadNotifications() {
        String email = currentUserService.getCurrentUserEmail();

        return notificationRepository
                .countByRecipientEmailAndReadFalse(email);
    }

    @Transactional
    @Override
    public void markMyNotificationAsRead(Long notificationId) {
        String email = currentUserService.getCurrentUserEmail();

        Notification notification = notificationRepository
                .findByIdAndRecipientEmail(notificationId, email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Notification not found"
                        )
                );

        if (notification.isRead()) {
            return;
        }

        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());

    }

    @Transactional
    @Override
    public void markAllMyNotificationsAsRead() {
        String email = currentUserService.getCurrentUserEmail();

        notificationRepository.markAllAsReadByRecipientEmail(
                email,
                LocalDateTime.now()
        );

    }






    private Notification buildNotification(NotificationCommand command) {
        return Notification.builder()
                .recipient(command.getRecipient())
                .typeNotification(command.getType())
                .title(command.getTitle())
                .message(command.getMessage())
                .referenceType(command.getReferenceType())
                .referenceId(command.getReferenceId())
                .read(false)
                .createdAt(LocalDateTime.now())
                .readAt(null)
                .build();
    }


    private void validateNotificationCommand(NotificationCommand command) {
        if (command == null) {
            throw new BusinessException("Notification command is required");
        }
        if (command.getRecipient() == null) {
            throw new BusinessException("Notification recipient is required");
        }
        if (command.getRecipient().getId() == null) {
            throw new BusinessException("Notification recipient must be persisted");
        }
        if (command.getType() == null) {
            throw new BusinessException("Notification type is required");
        }

        validateTitle(command.getTitle());
        validateMessage(command.getMessage());
        validateReference(command.getReferenceType(),
                command.getReferenceId());
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new BusinessException(
                    "Notification title is required"
            );
        }

        if (title.trim().length() > MAX_TITLE_LENGTH) {
            throw new BusinessException(
                    "Notification title must not exceed "
                            + MAX_TITLE_LENGTH
                            + " characters"
            );
        }
    }

    private void validateMessage(String message) {
        if (message == null || message.isBlank()) {
            throw new BusinessException(
                    "Notification message is required"
            );
        }

        if (message.trim().length() > MAX_MESSAGE_LENGTH) {
            throw new BusinessException(
                    "Notification message must not exceed "
                            + MAX_MESSAGE_LENGTH
                            + " characters"
            );
        }
    }

    private void validateReference(
            NotificationReferenceType referenceType,
            Long referenceId
    ) {
        boolean hasReferenceType = referenceType != null;
        boolean hasReferenceId = referenceId != null;

        if (hasReferenceType != hasReferenceId) {
            throw new BusinessException(
                    "Notification reference type and id must be provided together"
            );
        }

        if (referenceId != null && referenceId <= 0) {
            throw new BusinessException(
                    "Notification reference id must be positive"
            );
        }
    }
}


