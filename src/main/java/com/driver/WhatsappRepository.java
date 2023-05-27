package com.driver;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class WhatsappRepository {

    // User database
    private HashMap<String, User> userHashMap;

    // Group database
    private HashMap<Group, List<User>> groupHashMap;

    // Message database
    private HashMap<Integer, Message> messages;

    // Group Messages database
    private HashMap<Group, List<Message>> groupMessage;

    // User Messages database
    private HashMap<User, List<Message>> userMessage;

    // Group count
    private int groupCount = 0;

    // Message count
    private int messageCount = 0;

    public WhatsappRepository() {
        this.userHashMap = new HashMap<>();
        this.groupHashMap = new HashMap<>();
        this.messages = new HashMap<>();
        this.groupMessage = new HashMap<>();
        this.userMessage = new HashMap<>();
    }

    // Create a new user
    public String createUser(String name, String mobile) throws Exception {
        User user = new User(mobile, name);
        userHashMap.put(mobile, user);
        return "SUCCESS";
    }

    // Create a new message
    public int createMessage(String content) {
        messageCount++;
        Message message = new Message(messageCount, content);
        message.setTimestamp(new Date());
        messages.put(messageCount, message);
        return messageCount;
    }

    // Create a new group
    public Group createGroup(List<User> users) {
        if (users.size() == 2) {
            Group group = new Group(users.get(1).getName(), 2); // or get(0)
            groupHashMap.put(group, users);
            return group;
        } else {
            groupCount++;
            Group group = new Group("Group " + groupCount, users.size());
            groupHashMap.put(group, users);
            return group;
        }
    }

    // Send a message in a group
    public int sendMessageInGroup(Message message, User sender, Group group) throws Exception {
        if (!groupHashMap.containsKey(group)) {
            throw new Exception("Group does not exist");
        }
        if (!groupHashMap.get(group).contains(sender)) {
            throw new Exception("You are not allowed to send message");
        }

        if (!groupMessage.containsKey(group)) {
            List<Message> messages = new ArrayList<>();
            messages.add(message);
            groupMessage.put(group, messages);
        } else {
            groupMessage.get(group).add(message);
        }

        if (!userMessage.containsKey(sender)) {
            List<Message> messages = new ArrayList<>();
            messages.add(message);
            userMessage.put(sender, messages);
        } else {
            userMessage.get(sender).add(message);
        }

        return groupMessage.get(group).size();
    }

    // Remove a user from a group
    public int removeUser(User user) throws Exception {
        boolean userExist = false;
        boolean isAdmin = false;
        Group groupName = null;

        // Find the group and user
        for (Group group : groupHashMap.keySet()) {
            int num = 0;
            for (User user1 : groupHashMap.get(group)) {
                num++;
                if (user1.equals(user)) {
                    if (num == 1) {
                        isAdmin = true;
                    }
                    userExist = true;
                    groupName = group;
                    break;
                }
            }
            if (userExist) {
                break;
            }
        }

        if (!userExist) {
            throw new Exception("User not found");
        }
        if (isAdmin) {
            throw new Exception("Cannot remove admin");
        }

        List<Message> userMessages = userMessage.get(user);

        // Remove the messages sent by the user from the group and global message list
        for (Message message : userMessages) {
            messages.remove(message.getId());
            groupMessage.get(groupName).remove(message);
        }

        // Remove the user from the group
        groupHashMap.get(groupName).remove(user);

        // Remove user's messages
        userMessages.remove(user);

        // Return the total count of group members, group messages, and global messages
        return groupHashMap.get(groupName).size() + groupMessage.get(groupName).size() + messages.size();
    }

    // Change the group admin
    public String changeAdmin(User approver, User user, Group group) throws Exception {
        if (!groupHashMap.containsKey(group)) {
            throw new Exception("Group does not exist");
        }
        if (!approver.equals(groupHashMap.get(group).get(0))) {
            throw new Exception("Approver does not have rights");
        }

        boolean check = false;
        for (User user1 : groupHashMap.get(group)) {
            if (user1.equals(user)) {
                check = true;
            }
        }

        if (!check) {
            throw new Exception("User is not a participant");
        }

        User oldAdmin = groupHashMap.get(group).get(0);
        groupHashMap.get(group).set(0, user);
        groupHashMap.get(group).add(oldAdmin);

        return "SUCCESS";
    }

    // Find a message within a specified date range
    public String findMessage(Date start, Date end, int K) throws Exception {
        boolean latest = false;
        int k = 0;
        String message = null;

        // Iterate over messages in reverse order
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message message1 = messages.get(i);
            if (message1.getTimestamp().compareTo(start) > 0 && message1.getTimestamp().compareTo(end) < 0) {
                k++;
                if (k == 1) {
                    latest = true;
                }
                if (latest) {
                    message = message1.getContent();
                    latest = false;
                }
            }
        }

        if (k < K) {
            throw new Exception("K is greater than the number of messages");
        }

        return message;
    }
}
