package logReader.reader;

import logReader.common.LogEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pkazakov on 11/18/15.
 */
public class EventParser {
    public static LogEvent<String> parseEvent(Long time, String inputEvent){
        LogEvent<String> logEvent = new LogEvent<String>();
        logEvent.setTime(time);
        // parse src
        //int startSource = inputEvent.indexOf("] [")+3;
        //int endSource = inputEvent.indexOf("] [",startSource);
        Map.Entry<String,Integer> source = parse(inputEvent, 0, "] [","] [").entrySet().iterator().next();
        logEvent.setSource(source.getKey());

        // parse dest
        Map.Entry<String,Integer> dest = parse(inputEvent, source.getValue(), "] ["," {").entrySet().iterator().next();
        logEvent.setDest(dest.getKey());

        // parse ability
        Map.Entry<String,Integer> ability = parse(inputEvent, dest.getValue(), "] ["," {").entrySet().iterator().next();
        logEvent.setAbility(ability.getKey());

        // parse effect
        Map.Entry<String,Integer> effect = parse(inputEvent, ability.getValue(), "}: "," {").entrySet().iterator()
                .next();
        logEvent.setEffect(effect.getKey());

        // parse message
        Map.Entry<String,Integer> message = parse(inputEvent, effect.getValue(), ") <",">").entrySet().iterator()
                .next();
        logEvent.setMessage(message.getKey());
        return logEvent;
    }
    static Map<String,Integer> parse(String input,int from,String start,String end){
        Map<String,Integer> map = new HashMap<String,Integer>();
        int startDest = input.indexOf(start,from)+start.length();
        if( startDest < 0){
            map.put("",from);
            return map;
        }

        int endDest = input.indexOf(end,startDest);

        if( endDest < 0){
            map.put("",from);
            return map;
        }

        map.put(input.substring(startDest,endDest),endDest);
        return map;
    }

}
