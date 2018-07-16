package third.aliyun.edit.msg;

public interface MessageHandler {
    <T> int onHandleMessage(T message);
}
