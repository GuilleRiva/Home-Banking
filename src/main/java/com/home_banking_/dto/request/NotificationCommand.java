package com.home_banking_.dto.request;

import com.home_banking_.enums.NotificationReferenceType;
import com.home_banking_.enums.TypeNotification;
import com.home_banking_.model.Users;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationCommand {

    private Users recipient;
    private NotificationReferenceType referenceType;
    private TypeNotification type;
    private String message;
    private String title;
    private Long referenceId;

}
