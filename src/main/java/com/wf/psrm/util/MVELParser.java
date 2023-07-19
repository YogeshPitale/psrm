package com.wf.psrm.util;

import lombok.extern.slf4j.Slf4j;
import org.mvel2.MVEL;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Map;

@Slf4j
public class MVELParser {
    public static boolean parseMvelExpression( String expression, Map<String, Object> inputObjects){
        try {
            return MVEL.evalToBoolean(expression,inputObjects);
        }catch (Exception e){
            log.error("Can not parse Mvel Expression : {} Error: {}", expression, e.getMessage());
        }
        return false;
    }

    public static boolean executeScript(String expression, String input){
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine jsEngine = mgr.getEngineByName("JavaScript");
        try {
            jsEngine.eval("function test(abc){" + "var x=JSON.parse(abc);" + "return " + expression + "}");
            Invocable inv = (Invocable) jsEngine;
            boolean result = (Boolean) inv.invokeFunction("test", input);
            return result;
        } catch (ScriptException ex) {
            ex.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return false;
    }


}
