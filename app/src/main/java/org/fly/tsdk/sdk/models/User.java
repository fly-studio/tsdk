package org.fly.tsdk.sdk.models;

import org.fly.core.annotation.NotProguard;
import org.fly.core.text.json.Jsonable;

@NotProguard
public class User {
    public static class Base extends Jsonable {
        public Property property;
        public long uid;
        public long auid;
    }

    public static class Register extends Jsonable {
        public Property property;
        public String username = null;
        public String password = null;

    }

    public static class Login extends Jsonable {
        public Property property;
        public String username = null;
        public String password = null;
    }

    public static class Verify extends Base {
        public String at;
        public String sign;
    }

    public static class GenerateUsername extends Jsonable {
        public Property property;

    }

    public static class Logout extends Base {

    }


}
