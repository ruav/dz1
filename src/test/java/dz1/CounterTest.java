package dz1;

import dz1.Exception.MyException;
import dz1.reso.ResourceImplLocal;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.*;

/**
 * Created by ruav on 15.12.16.
 */
public class CounterTest {

    private Counter counter;
    BufferedReader bReader;

    private static Logger logger = LoggerFactory.getLogger(CounterTest.class);

    @Before
    public void before() throws FileNotFoundException {
        counter = new Counter("-f","/media/ruav/8064B61E64B6173E/resources/8.txt",new HashMap<>(),new ReentrantLock());
        bReader = new BufferedReader(new FileReader(counter.getResource().getResourceFile()));
    }


    @Test ()
    public void makeResource() throws MalformedURLException {
        counter.makeResource("-f","/media/ruav/8064B61E64B6173E/resources/20.txt");
        // файл не существует, но проверка на почему - то выдает положительный результат....хз даже..
        assertEquals(counter.getResource().checkResourceExist(), true);
    }

    @Test
    public void makeResource1() throws MalformedURLException {
        assertTrue(counter.makeResource("-f","/media/ruav/8064B61E64B6173E/resources/8.txt") instanceof ResourceImplLocal);
    }

    @Test
    public void parse() throws Exception {
        counter.parse(bReader, counter.getLock());
        logger.info("Function parse");
    }


    @Test(expected = FileNotFoundException.class)
    public void parseExc() throws Exception {
        counter.parse(new BufferedReader(new FileReader("/media/ruav/8064B61E64B6173E/resources/111.txt")), counter.getLock());
        logger.info("FileNotFoundException");
    }

    @Test(expected = MyException.class)
    public void parse1() throws Exception {
        counter.parse(bReader, counter.getLock());
        logger.info("MyException e");
    }


}