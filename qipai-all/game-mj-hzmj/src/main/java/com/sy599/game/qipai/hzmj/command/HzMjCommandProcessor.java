package com.sy599.game.qipai.hzmj.command;

import java.util.HashMap;
import java.util.Map;

import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.qipai.AbstractBaseCommandProcessor;
import com.sy599.game.qipai.hzmj.bean.HzMjPlayer;
import com.sy599.game.qipai.hzmj.command.com.HzMjComCommand;
import com.sy599.game.qipai.hzmj.command.play.HzMjPlayCommand;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class HzMjCommandProcessor extends AbstractBaseCommandProcessor {
    private static HzMjCommandProcessor processor = new HzMjCommandProcessor();
    private static Map<Short, Class<? extends BaseCommand>> commandMap = new HashMap<>();
    private static Map<Class<?>, Short> msgClassToMsgTypeMap = new HashMap<Class<?>, Short>();

    static {
        commandMap.put(WebSocketMsgType.cs_com, HzMjComCommand.class);
        commandMap.put(WebSocketMsgType.cs_play, HzMjPlayCommand.class);

        try {
            for (Short type : commandMap.keySet()) {
                Class<? extends BaseCommand> cl = commandMap.get(type);
                BaseCommand action = cl.newInstance();
                Map<Class<?>, Short> msgTypeMap = action.getMsgTypeMap();
                if (msgTypeMap != null && !msgTypeMap.isEmpty()) {
                    for (Class<?> msgClass : msgTypeMap.keySet()) {
                        if (msgClassToMsgTypeMap.containsKey(msgClass)) {
                            throw new Exception("msgClassToMsgTypeMap err!!!!");

                        } else {
                            msgClassToMsgTypeMap.put(msgClass, msgTypeMap.get(msgClass));

                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.e("SocketAcitonProcessor err:", e);

        }

    }

    public static HzMjCommandProcessor getInstance() {
        return processor;
    }

    @Override
    public void process(Player player, MessageUnit message) {
        int code = 0;
        try {
            HzMjPlayer _player = player.getPlayer(processor);
            LogUtil.msgLog.info(player.getUserId() + " msg:" + message.getMsgType());
            BaseCommand action = commandMap.get((short) message.getMsgType()).newInstance();
            action.setPlayer(player);
            action.execute(_player, message);
        } catch (Exception e) {
            LogUtil.e("socket err: " + player.getUserId() + " " + message.getMsgType(), e);
            code = -1;
        } finally {
            if (code != 0) {
            }
        }
    }

    public short getMsgType(Class<?> clazz) {
        if (msgClassToMsgTypeMap.containsKey(clazz)) {
            return msgClassToMsgTypeMap.get(clazz);
        }
        return 0;
    }

}