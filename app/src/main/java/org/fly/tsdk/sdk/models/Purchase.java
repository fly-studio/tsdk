package org.fly.tsdk.sdk.models;

import com.fasterxml.jackson.annotation.JsonRawValue;

import org.fly.core.annotation.NotProguard;
import org.fly.core.text.json.Jsonable;

import java.math.BigDecimal;

@NotProguard
public class Purchase {
    public static class Pay extends User.Base {
        public String item_name = null;
        public String cp_order_no = null;
        public BigDecimal amount = new BigDecimal(0);
        @JsonRawValue
        public String order_params = null;
    }

    public static class Paid extends User.Base {
        public long oid = 0;
    }

    public static class CancelPay extends User.Base {
        public long oid = 0;
    }

}
