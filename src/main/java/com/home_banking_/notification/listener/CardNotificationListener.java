package com.home_banking_.notification.listener;

import com.home_banking_.dto.request.NotificationCommand;
import com.home_banking_.enums.NotificationReferenceType;
import com.home_banking_.enums.TypeNotification;
import com.home_banking_.event.card.CardCreatedEvent;
import com.home_banking_.exceptions.custom.ResourceNotFoundException;
import com.home_banking_.model.Users;
import com.home_banking_.repository.UsersRepository;
import com.home_banking_.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class CardNotificationListener {

    private final NotificationService notificationService;
    private final UsersRepository usersRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCardCreated(CardCreatedEvent event) {
        Users recipient = usersRepository.findById(event.recipientId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Notification recipient not found"
                        )
                );

        NotificationCommand command = new NotificationCommand(
                recipient,
                NotificationReferenceType.CARD,
                TypeNotification.CARD_CREATED,
                buildCreatedMessage(event),
                "Your card was created",
                event.cardId()
        );

        notificationService.notifyUser(command);

        log.info(
                "[CARD_CREATED_NOTIFICATION_SUCCESS] recipientId={} cardId={}",
                event.recipientId(),
                event.cardId()
        );
    }

    private String buildCreatedMessage(CardCreatedEvent event) {
        return String.format(
                "Your %s card from %s was created successfully.",
                event.typeCard(),
                event.cardBrand()
        );
    }


}
