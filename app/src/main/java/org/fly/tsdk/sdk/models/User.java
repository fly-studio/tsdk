package org.fly.tsdk.sdk.models;

import org.fly.core.annotation.NotProguard;
import org.fly.core.text.json.Jsonable;

@NotProguard
final public class User {
    public static class Base extends Jsonable {
        public Property property;
        public long uid;
        public long auid;
    }

    final public static class Register extends Jsonable {
        public Property property;
        public String username = null;
        public String password = null;

    }

    final public static class Login extends Jsonable {
        public Property property;
        public String username = null;
        public String password = null;
    }

    final public static class Verify extends Base {
        public String at;
        public String sign;
    }

    final public static class GenerateUsername extends Jsonable {
        public Property property;

    }

    final public static class Logout extends Base {

    }


}
