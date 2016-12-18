package dz1.utils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by ruav on 18.12.16.
 */
public class ValidatorTest {
    @Test
    public void validate() throws Exception {
        assertTrue("",Validator.validate("dsghw"));
        assertFalse("",Validator.validate("мама мыла раму"));
    }

//    @Test
//    public void validate1() throws Exception{
//        assertFalse("",Validator.validate("мама мыла раму"));
//    }

}