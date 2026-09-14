package com.bom.bom.service;
import java.util.*;
public class UsageConditionEvaluator {
 public boolean matches(String expression, Map<String,String> selections){if(expression==null||expression.trim().isEmpty())return true;for(String or:expression.split("\\|")){boolean ok=true;for(String and:or.split("&")){String t=and.trim();boolean neq=t.contains("!=");String[] p=t.split(neq?"!=":"=",2);if(p.length!=2||selections==null||!selections.containsKey(p[0].trim())){ok=false;break;}boolean same=p[1].trim().equals(selections.get(p[0].trim()));if(neq?same:!same){ok=false;break;}}if(ok)return true;}return false;}
 public static boolean evaluate(String e,Map<String,String>s){return new UsageConditionEvaluator().matches(e,s);}
}
