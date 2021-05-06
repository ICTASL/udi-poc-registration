package io.mosip.registration.util.common;

import com.asankha.translit.Transliterate;
import io.mosip.registration.context.ApplicationContext;

public class TranslitUtil {

    private static final String ENG_LOCALE = "eng";
    private static final String SINHALA_LOCALE = "sin";
    private static final String TAMIL_LOCALE = "tam";

    public String transliterate(int fromLanguage, int toLanguage, String inputText) {
        String transliteratedResult = Transliterate.translateWord(inputText, fromLanguage, toLanguage, Transliterate.UNKNOWN);
        transliteratedResult = processInputText(inputText, transliteratedResult);
        return transliteratedResult;
    }

    public int getSourceLocale() {
        int srcLocaleInt = getTranslitLocale(ApplicationContext.applicationLanguage());
        return srcLocaleInt;
    }

    public int getTargetLocale() {
        int targetLocaleInt = getTranslitLocale(ApplicationContext.localLanguage());
        return targetLocaleInt;
    }

    private int getTranslitLocale(String languageVal) {
        int translitLocale = Transliterate.ENGLISH;
        if(!languageVal.equals(ENG_LOCALE)){
            if(languageVal.equals(SINHALA_LOCALE)){
                translitLocale = Transliterate.SINHALA;
            } else if (languageVal.equals(TAMIL_LOCALE)) {
                translitLocale = Transliterate.TAMIL;
            }
        }
        return translitLocale;
    }

    private String processInputText (String input_text, String transliterated_text) {
        String result = processLeading_I(input_text, transliterated_text);
        result = process_KA(input_text, result);
        result = process_LLA(input_text, result);

        return  result;
    }

    private String processLeading_I(String in, String out){
        String result = out;

        if( in.charAt(0) == '\u0B87' && in.charAt(1) == '\u0BB0'){
            result = out.substring (1, out.length());
        }
        return result;
    }

    private String process_KA(String in, String out){
        String result = out;
        char temp [] = in.toCharArray();

        for(int i =0; i < temp.length; i++){
            if(temp[i] == '\u0B95' && i>0 && temp[i+1]!='\u0BCD' && temp[i-1]!=' ' && temp[i-1]!='\u0BCD' ){
                result = result.replace('\u0DC4', '\u0D9C');
            }
        }
        return result;
    }

    private String process_LLA(String in, String out){
        String result = out;
        char temp [] = in.toCharArray();

        for(int i =0; i < temp.length; i++){
            if(temp[i] == '\u0BB3'){
                result = result.replace('\u0DC5', '\u0DBD');
            }
        }
        return result;
    }
}
