package pl.mareklangiewicz.myutils;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.support.annotation.NonNull;

import com.noveogroup.android.log.MyLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import com.google.common.base.MoreObjects;
//import org.javatuples.KeyValue;

/**
 * Created by Marek Langiewicz on 29.09.15.
 * <p>
 * TODO LATER: generate and use compiled versions of regular expressions..
 */
public final class MyCommands {

    public static final String CMD_ACTIVITY = "activity";
    public static final String CMD_SERVICE = "service";
    public static final String CMD_BROADCAST = "broadcast";
    public static final String CMD_FRAGMENT = "fragment";
    /**
     * $1: id
     */
    public static final String RE_ID = "([_a-zA-Z][_a-zA-Z0-9]*)";
    /**
     * $1: whole multi id (a multipart ident. divided by dots and/or slashes; can start with dot)
     * $2: id (first part - until first divider)
     * $3: rest (all after id - starting with divider - if not empty)
     * $4: subid (last part of multi id - after last divider)
     */
    public static final String RE_MULTI_ID = "(\\.?" + RE_ID + "((?:(?:\\.|/)" + RE_ID + ")*))";
    /**
     * $1: datatype
     */
    public static final String RE_EXTRA_TYPE = "((?:string)|(?:boolean)|(?:byte)|(?:char)|(?:double)|(?:float)|(?:integer)|(?:long)|(?:short))";
    /**
     * $1: keyword
     */
    public static final String RE_KEYWORD = "((?:start)|(?:action)|(?:category)|(?:type)|(?:data)|(?:flags)|(?:package)|(?:component)|(?:scheme)|(?:bounds)|" +
            "(?:extra))";
    /**
     * $1: value
     */
    public static final String RE_VALUE = "(.*?)";

    //    public static @NonNull <T> T valOrDef(@Nullable T val,@NonNull T def){ return MoreObjects.firstNonNull(val, def); }
//    public static @NonNull <T> T mapOrNot(@NonNull T val, @NonNull Map<T, T> map) { return valOrDef(map.get(val), val); }
    public static final String RE_END = "(?: |\\Z)";
    /**
     * $1: segment
     * $2: keyword
     * $3: value
     * $4: next keyword or the end (empty string) - probably not important for us.
     */
    public static final String RE_SEGMENT = "(" + RE_KEYWORD + " " + RE_VALUE + RE_END + "(?=\\Z|" + RE_KEYWORD + "))";
    /**
     * $1: whole key
     * $2: first part - until first dot
     * $3: rest
     * $4: last part - not important here..
     */
    public static final String RE_EXTRA_KEY = RE_MULTI_ID;
    /**
     * $1: whole extra elem
     * $2: datatype
     * $3: extra key
     * $4: extra key - first part - not important
     * $5: extra key - the rest - not important
     * $6: extra key - last part - not important
     * $7: value
     */
    public static final String RE_EXTRA_ELEM = "(" + RE_EXTRA_TYPE + " " + RE_EXTRA_KEY + " " + RE_VALUE + ")";
    /**
     * $1: whole thing
     * $2: datatype
     * $3: extra key
     * $4: extra key - first part - not important
     * $5: extra key - the rest - not important
     * $6: extra key - last part - not important
     */
    public static final String RE_EXTRA_ELEM_TYPE_AND_KEY = "(" + RE_EXTRA_TYPE + " " + RE_EXTRA_KEY + ")";
    static public final List<String> DEFAULT_EXAMPLE_COMMANDS = Arrays.asList(
            "action view data http://mareklangiewicz.pl",
            "action view data mareklangiewicz.pl",
            "wake me up at 7",
            "set alarm to 7:30",
            "set an alarm at 8 30",
            "set a timer for 300",
            "set timer for 200 seconds",
            "set timer for 200 seconds quickly",
            "fragment .MIHelpFragment",
            "listen"
    );

    private static final String EX_HOUR = AlarmClock.EXTRA_HOUR;
    private static final String EX_MINUTES = AlarmClock.EXTRA_MINUTES;
    private static final String EX_MESSAGE = AlarmClock.EXTRA_MESSAGE;
    private static final String EX_LENGTH = "android.intent.extra.alarm.LENGTH";
    private static final String EX_SKIP_UI = AlarmClock.EXTRA_SKIP_UI;
    private static final String ACT_SET_ALARM = AlarmClock.ACTION_SET_ALARM;
    private static final String ACT_SET_TIMER = "android.intent.action.SET_TIMER";
    static public final List<REGroup> RE_RULES = Arrays.asList(

            new REGroup("initial", "^.+",
                    new RERule("(?:\\s|;|=)+", " ") // change ; and = to spaces (set it to only one in a row)
            ),

            new REGroup("user", "^.+"),

            new REGroup("alarm", "^((wake me)|(set( an | the | )alarm))",
                    new RERule("^wake me( up)?", "set alarm"),
                    new RERule("^set (an|the) alarm", "set alarm"),
                    new RERule("^set alarm ((for)|(to)|(at))", "set alarm"),
                    new RERule("^set alarm (\\d+)(?::| )(\\d+)", "set alarm extra hour $1 extra minutes $2"),
                    new RERule("^set alarm (\\d+)", "set alarm extra hour $1"),
                    new RERule("^set alarm", "action set alarm"),
                    new RERule("\\bextra hour (\\d+)\\b", "extra integer " + EX_HOUR + " $1"),
                    new RERule("\\bextra minutes (\\d+)\\b", "extra integer " + EX_MINUTES + " $1"),
                    new RERule("^action set alarm (.*) with message (.*?)$", "action set alarm $1 extra string " + EX_MESSAGE + " $2"),
                    new RERule("(.*) quickly$", "$1 extra boolean " + EX_SKIP_UI + " true")
            ),

            new REGroup("timer", "^((set timer)|(set a timer))",
                    new RERule("^set a timer", "set timer"),
                    new RERule("^set timer ((for)|(to)|(at))", "set timer"),
                    new RERule("^set timer (\\d+)( seconds)?", "set timer extra length $1"),
                    new RERule("^set timer", "action set timer"),
                    new RERule("\\bextra length (\\d+)\\b", "extra integer " + EX_LENGTH + " $1"),
                    new RERule("^action set timer (.*) with message (.*?)$", "action set timer $1 extra string " + EX_MESSAGE + " $2"),
                    new RERule("(.*) quickly$", "$1 extra boolean " + EX_SKIP_UI + " true")
            ),

            new REGroup("activity/fragment", "^((activity)|(fragment))",
                    new RERule("^((?:activity)|(?:fragment)) " + RE_KEYWORD, "start $1 $2"), //no keyword can be activity or fragment class name..
                    new RERule("^((?:activity)|(?:fragment)) " + RE_MULTI_ID, "start $1 component $2"),
                    new RERule("^((?:activity)|(?:fragment)) ", "start $1 ")
            ),

            new REGroup("ending", "^.+", // last rules for all nonempty commands:
                    new RERule("\\baction view\\b", "action " + Intent.ACTION_VIEW),
                    new RERule("\\baction send\\b", "action " + Intent.ACTION_SEND),
                    new RERule("\\baction set alarm\\b", "action " + ACT_SET_ALARM),
                    new RERule("\\baction set timer\\b", "action " + ACT_SET_TIMER)
            )

    );
    /*
        private static final boolean V = BuildConfig.VERBOSE;
        private static final boolean VV = BuildConfig.VERY_VERBOSE;

        FIXME SOMEDAY: enable version with BuildConfig when Google fix issue with propagating build types to libraries.
        Now it is always 'release' in libraries.. see:
        https://code.google.com/p/android/issues/detail?id=52962
        http://stackoverflow.com/questions/20176284/buildconfig-debug-always-false-when-building-library-projects-with-gradle
        http://tools.android.com/tech-docs/new-build-system/user-guide#TOC-Library-Publication
    */
    private static final boolean V = true;
    private static final boolean VV = false;
    static boolean sUT = false; // FIXME LATER: this is temporary hack to detect unit tests.. remove it.
    private MyCommands() {
        throw new AssertionError("MyCommands class is noninstantiable.");
    }

    static public void parseCommand(String in, Map<String, String> out) {
        int length = in.length();
        Pattern pattern = Pattern.compile(RE_SEGMENT);
        Matcher matcher = pattern.matcher(in);
        while(true) {
            boolean ok = matcher.lookingAt();
            if(!ok)
                throw new IllegalArgumentException("Illegal command format: " + in);

            int N = matcher.groupCount();

//            for(int i = 0; i <= N; ++i) log.d("%d: %s", i, matcher.group(i));

            if(N != 4)
                throw new InternalError();

            String keyword = matcher.group(2);
            String value = matcher.group(3);

            if(!sUT) { // FIXME LATER: this 'if' is temporary hack
                keyword = Uri.decode(keyword); // TODO LATER: analyse if this is the best place do Uri.decode (propably yes)
                value = Uri.decode(value); // TODO LATER: analyse if this is the best place to Uri.decode (probably yes)
            }

            switch(keyword) {
                case "extra":
                    parseCommandExtraSegment(value, out);
                    break;
                case "flags":
                    out.put(keyword, value);
                    //TODO LATER: what about multiple flags? add some symbolic multiple flags implementation
                    break;
                default:
                    out.put(keyword, value);
            }

            int end = matcher.end();
            if(end == length)
                break;
            matcher.region(end, length);
        }

    }

    static public void parseCommandExtraSegment(String extra, Map<String, String> out) {
        Pattern pattern = Pattern.compile(RE_EXTRA_ELEM);
        Matcher matcher = pattern.matcher(extra);
        boolean ok = matcher.matches();
        if(!ok)
            throw new IllegalArgumentException("Illegal extra segment: " + extra);
        int N = matcher.groupCount();
        if(N != 7)
            throw new InternalError();

        String type = matcher.group(2);
        String key = matcher.group(3);
        String value = matcher.group(7);

        out.put("extra " + type + " " + key, value);

    }

    public static void setIntentFromCommand(Intent intent, Map<String, String> cmd, MyLogger log) {

        for(String key : cmd.keySet()) {
            String value = cmd.get(key);
            switch(key) {
                case "start":
                    // we don't care here. Should be checked on higher level
                    break;
                case "action":
                    intent.setAction(value);
                    break;
                case "category":
                    intent.addCategory(value);
                    break;
                case "type":
                    Uri data = intent.getData();
                    if(data == null)
                        intent.setType(value);
                    else
                        intent.setDataAndType(data, value);
                    break;
                case "data":
                    String type = intent.getType();
                    if(type == null)
                        intent.setData(Uri.parse(value));
                    else
                        intent.setDataAndType(Uri.parse(value), type);
                    break;
                case "flags":
                    intent.addFlags(Integer.decode(value)); //TODO LATER: symbolic multiple flags implementation
                    break;
                case "package":
                    intent.setPackage(value);
                    break;
                case "component":
                    ComponentName cn = ComponentName.unflattenFromString(value);
                    if(cn == null)
                        log.e("Illegal component name: %s", value);
                    intent.setComponent(cn);
                    break;
                case "scheme":
                    intent.setData(Uri.parse(value + ":"));
                    break;
                case "bounds":
                    intent.setSourceBounds(Rect.unflattenFromString(value));
                    break;
                default:
                    if(key.startsWith("extra "))
                        setIntentExtra(intent, key.substring("extra ".length()), value);
                    else
                        throw new IllegalArgumentException("Illegal intent parameter:" + key);
            }
        }
    }

    private static void setIntentExtra(Intent intent, String key, String value) {
        Pattern pattern = Pattern.compile(RE_EXTRA_ELEM_TYPE_AND_KEY);
        Matcher matcher = pattern.matcher(key);
        boolean ok = matcher.matches();
        if(!ok)
            throw new IllegalArgumentException("Illegal extra type or key: " + key);
        int N = matcher.groupCount();
        if(N != 6)
            throw new InternalError();

        String type = matcher.group(2);
        key = matcher.group(3); // now key contains only key (no type)

        switch(type) {
            case "string":
                intent.putExtra(key, value);
                break;
            case "boolean":
                intent.putExtra(key, Boolean.parseBoolean(value));
                break;
            case "byte":
                intent.putExtra(key, Byte.parseByte(value));
                break;
            case "char":
                intent.putExtra(key, value.charAt(0));
                break;
            case "double":
                intent.putExtra(key, Double.parseDouble(value));
                break;
            case "float":
                intent.putExtra(key, Float.parseFloat(value));
                break;
            case "integer":
                intent.putExtra(key, Integer.parseInt(value));
                break;
            case "long":
                intent.putExtra(key, Long.parseLong(value));
                break;
            case "short":
                intent.putExtra(key, Short.parseShort(value));
                break;
            default:
                throw new IllegalArgumentException("Illegal extra segment type: " + type);
        }
    }

    public static void setBundleFromCommandExtras(Bundle bundle, Map<String, String> cmd) {

        for(String key : cmd.keySet()) {
            String value = cmd.get(key);
            if(key.startsWith("extra ")) {
                setBundleFromExtra(bundle, key.substring("extra ".length()), value);
            }
        }
    }

    private static void setBundleFromExtra(Bundle bundle, String key, String value) {
        Pattern pattern = Pattern.compile(RE_EXTRA_ELEM_TYPE_AND_KEY);
        Matcher matcher = pattern.matcher(key);
        boolean ok = matcher.matches();
        if(!ok)
            throw new IllegalArgumentException("Illegal extra type or key: " + key);
        int N = matcher.groupCount();
        if(N != 6)
            throw new InternalError();

        String type = matcher.group(2);
        key = matcher.group(3); // now key contains only key (no type)

        switch(type) {
            case "string":
                bundle.putString(key, value);
                break;
            case "boolean":
                bundle.putBoolean(key, Boolean.parseBoolean(value));
                break;
            case "byte":
                bundle.putByte(key, Byte.parseByte(value));
                break;
            case "char":
                bundle.putChar(key, value.charAt(0));
                break;
            case "double":
                bundle.putDouble(key, Double.parseDouble(value));
                break;
            case "float":
                bundle.putFloat(key, Float.parseFloat(value));
                break;
            case "integer":
                bundle.putInt(key, Integer.parseInt(value));
                break;
            case "long":
                bundle.putLong(key, Long.parseLong(value));
                break;
            case "short":
                bundle.putShort(key, Short.parseShort(value));
                break;
            default:
                throw new IllegalArgumentException("Illegal extra segment type: " + type);
        }
    }

    static public final class RERule {

        private String mMatch;
        private Pattern mPattern;
        private String mReplace;

        public RERule(@NonNull String match, @NonNull String replace) {
            setMatch(match);
            setReplace(replace);
        }

        static public @NonNull String applyAll(@NonNull Iterable<RERule> rules, @NonNull String cmd, @NonNull MyLogger log) {
            for(RERule rule : rules) {
                cmd = rule.apply(cmd, log);
            }
            return cmd;
        }

        public @NonNull String getMatch() { return mMatch; }

        public void setMatch(@NonNull String match) {
            mMatch = match;
            mPattern = Pattern.compile(match);
        }

        public @NonNull String getReplace() { return mReplace; }

        public void setReplace(@NonNull String replace) { mReplace = replace; }

        @Override public String toString() {
            return mMatch + " -> " + mReplace;
        }

        /**
         * Checks if the rule matches ANYWHERE in given command
         */
        public boolean matches(@NonNull String cmd) {
            return mPattern.matcher(cmd).find(0);
        }

        /**
         * Applying a rule means matching and replacing ALL occurrences of re mMatch with mReplace
         */
        public @NonNull String apply(@NonNull String cmd, @NonNull MyLogger log) {

            Matcher matcher = mPattern.matcher(cmd);

            if(matcher.find(0)) {
                cmd = matcher.replaceAll(mReplace);
                if(V) {
                    log.v("rule matched:");
                    log.d("rule: %s", toString());
                    log.i("= cmd: %s", cmd);
                }
                return cmd;
            }
            else {
                if(VV) {
                    log.v("rule NOT matched:");
                    log.v("rule: %s", toString());
                    log.v("= cmd: %s", cmd);
                }
                return cmd;
            }
        }
    }

    static public final class REGroup {

        private String mName;
        private String mMatch;
        private Pattern mPattern;
        private List<RERule> mRules;

        public REGroup(@NonNull String name, @NonNull String match, RERule... rules) {
            setName(name);
            setMatch(match);
            mRules = new ArrayList<>(rules.length);
            mRules.addAll(Arrays.asList(rules));
        }

        static public @NonNull String applyAll(@NonNull Iterable<REGroup> groups, @NonNull String cmd, @NonNull MyLogger log) {

            if(V) {
                log.v("Applying all matching RE rules to:");
                log.w("> cmd: %s", cmd);
            }
            else
                log.v("> cmd: %s", cmd);

            for(REGroup group : groups) {
                cmd = group.apply(cmd, log);
            }

            if(V) {
                log.v("All matching RE rules applied. Result:");
                log.i("< cmd: %s", cmd);
            }
            else
                log.v("< cmd: %s", cmd);

            return cmd;
        }

        public @NonNull String getName() { return mName; }

        public void setName(@NonNull String name) { mName = name; }

        public @NonNull String getMatch() { return mMatch; }

        public void setMatch(@NonNull String match) {
            mMatch = match;
            mPattern = Pattern.compile(match);
        }

        public @NonNull List<RERule> getRules() { return mRules; }

        @Override public String toString() {
            return mName + ": " + mMatch;
        }

        /**
         * Checks if the group match field matches ANYWHERE in given command
         */
        public boolean matches(@NonNull String cmd) {
            return mPattern.matcher(cmd).find(0);
        }

        /**
         * It first check if the group match field matches ANYWHERE in given command, and if it does:
         * It applies all rules in this group one by one in order to given command.
         * Otherwise it just returns given command.
         */
        public @NonNull String apply(@NonNull String cmd, MyLogger log) {

            if(!matches(cmd)) {
                if(VV) {
                    log.v("group NOT matched:");
                    log.v("group: %s", toString());
                }
                return cmd;
            }

            if(V) {
                log.v("group matched:");
                log.d("group: %s", toString());
            }
            return RERule.applyAll(getRules(), cmd, log);
        }
    }
}