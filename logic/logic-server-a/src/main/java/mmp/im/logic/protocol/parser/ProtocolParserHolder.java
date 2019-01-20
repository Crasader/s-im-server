package mmp.im.logic.protocol.parser;


import mmp.im.common.protocol.parser.IMQProtocolParser;
import mmp.im.common.util.reflect.PackageUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ProtocolParserHolder {

    private static HashMap<Integer, Object> parsers;

    private static HashMap getParsers() {
        /*
         * DCL
         * */
        if (parsers == null) {

            synchronized (ProtocolParserHolder.class) {
                if (parsers == null) {
                    parsers = new HashMap<>();
                    List<Class<?>> classList = PackageUtil.getSubClasses("mmp.im.logic.protocol.parser", IMQProtocolParser.class);
                    Iterator iterator = classList.iterator();

                    while (iterator.hasNext()) {
                        Class c = (Class) iterator.next();
                        try {
                            IMQProtocolParser e = (IMQProtocolParser) c.newInstance();
                            parsers.put(e.getProtocolKind(), e);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }
        return parsers;
    }


    public static IMQProtocolParser get(int protocolKind) {
        return (IMQProtocolParser) getParsers().get(protocolKind);
    }
}
