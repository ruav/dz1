package dz1;

import dz1.Exception.MyException;
import dz1.reso.ResourceImplLocal;
import dz1.reso.Resource;
import dz1.reso.ResourceImplRemote;
import dz1.utils.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.locks.Lock;

/**
 * Created by RuAV on 09.12.2016.
 */
public class Counter implements Runnable{

    private String str;
    private String fileNameForParse;
    private Map<String, Integer> map;
    private Object monitor;
    private Lock lock;
    private File fileForParse;
    private Resource resource;

    private Logger logger = LoggerFactory.getLogger(Counter.class);

    public Counter(){

    }

    /**
     * Конструктор для анализа содержимого входного ресурса
     * @param prefix задает тип входящего ресурса
     * @param fileNameForParse имя ресурса
     * @param map хранилище для записи данных о содержимом
     * @param monitor монитор для блокировок
     */
    public Counter(String prefix, String fileNameForParse, Map<String, Integer> map, Object monitor) {
        this.fileNameForParse = fileNameForParse;
        this.map = map;
        this.monitor = monitor;
        try {
            resource = makeResource(prefix,fileNameForParse);
        } catch (MalformedURLException e) {
            logger.warn("Resource не может быть создан");
        }
    }

    /**
     * Конструктор для анализа содержимого входного ресурса
     * @param prefix задает тип входящего ресурса
     * @param fileNameForParse имя ресурса
     * @param map хранилище для записи данных о содержимом
     * @param lock монитор для блокировок
     */
    public Counter(String prefix, String fileNameForParse, Map<String, Integer> map, Lock lock) {
        this.fileNameForParse = fileNameForParse;
        this.map = map;
        this.lock = lock;
        try {
            resource = makeResource(prefix,fileNameForParse);
        } catch (MalformedURLException e) {
            logger.warn("Resource не может быть создан");
        }
    }

    @Override
    public void run() {
            try {
                BufferedReader bReader = new BufferedReader(new FileReader(resource.getResourceFile()));
                MDC.put("fileName",fileNameForParse);

                // только если у нас создался ресурс, приступаем к его вычитыванию.
                if(resource != null) {
                    if (lock != null) {
                        parse(bReader, lock);
                    } else {
                        parse(bReader, monitor);
                    }
                } else {
                    logger.warn("Поток не запущен");
                }
            }catch (FileNotFoundException e) {
                logger.warn(fileNameForParse + " FileNotFound");
            }catch (MyException e){
                logger.warn("Содержится латиница. Поток закрыт");
            }finally {
                MDC.clear();
            }
        }

    /**
     *
     * Метод парсит по - строчно файл на наличие русских букв, и ведет подсчет повторяемых слов.
     *
     * @param bReader Буферизованный поток, читающий  по - строково файл
     * @param monitor - Объект для блокировки нитей
     */
    public void parse(BufferedReader bReader, Object monitor) throws MyException{
        try {
            while (!Thread.currentThread().isInterrupted() && bReader.ready() && !Controller.breakAll) {
                str = bReader.readLine();

                // если переменная взведена, то сразу обрубаем поток
                if(Controller.breakAll)
                    return;

                // проверка на латиницу. Если есть латиница, то генерируем общий останов.
                if(Validator.validate(str)) {
                    Controller.breakAll = true;
                    logger.warn(fileNameForParse + " contains english words");
                    synchronized (monitor) {
                        map.clear();
                    }
                    logger.warn("Файлы содержат латиницу, поэтому статистика не верна.");
                    System.out.println("Файлы содержат латиницу, поэтому статистика не верна.");
                    throw new MyException("Thread is over!");
                }

                // убираем лишние символы
                str = str.replaceAll("[^а-яА-Я]"," ").replaceAll(" {2,}", " ");
                String[] strArray = str.split(" ");
                for (String s : strArray) {
                    System.out.println(Controller.breakAll);
                    if (s.isEmpty())
                        continue;
                    if(Controller.breakAll)
                        return;
                    synchronized (monitor) {
                        // забиваем мап данными о количестве слов
                        int count;
                        if (map.containsKey(s)) {
                            count = map.get(s) + 1;
                        } else {
                            count = 1;
                        }
                        map.put(s, count);
                        logger.info(s + " : " + count);
                        System.out.println(s + " : " + count);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            logger.warn("FileNotFoundException");
        } catch (IOException e) {
            logger.warn("IOException");
        }

    }


    /**
     *
     * Метод парсит по - строчно файл на наличие русских букв, и ведет подсчет повторяемых слов.
     *
     * @param bReader Буферизованный поток, читающий файл построчно
     * @param lock - Объект для блокировки нитей
     */
    public void parse(BufferedReader bReader, Lock lock) throws MyException{
        try {
            while (!Thread.currentThread().isInterrupted() && bReader.ready() && !Controller.breakAll) {
                str = bReader.readLine();

                // если переменная взведена, то сразу обрубаем поток
                if(Controller.breakAll) {
                    map.clear();
                    return;
                }
                // проверка на латиницу. Если есть латиница, то генерируем общий останов.
                if(Validator.validate(str)) {
                    try {
                        lock.lock();
                        Controller.breakAll = true;
                        logger.warn(fileNameForParse + " contains english words");
                        map.clear();
                        logger.warn("Файлы содержат латиницу, поэтому статистика не верна.");
                        System.out.println("Файлы содержат латиницу, поэтому статистика не верна.");
                        throw new MyException("Thread is over!");
                    }catch (Exception e){

                    }finally {
                        lock.unlock();
                    }
                }

                // убираем лишние символы
                str = str.replaceAll("[^а-яА-Я]"," ").replaceAll(" {2,}", " ");
                String[] strArray = str.split(" ");
                for (String s : strArray) {
                    if (s.isEmpty())
                        continue;
                    if(Controller.breakAll)
                        return;
                    // забиваем мап данными о количестве слов
                    try {
                        lock.tryLock();
                        int count;
                        if (map.containsKey(s)) {
                            count = map.get(s) + 1;
                        } else {
                            count = 1;
                        }
                        map.put(s, count);
                        System.out.println(s + " : " + count);
                    }catch (Exception e){

                    }finally {

                        lock.unlock();
                    }

                }
            }
        } catch (FileNotFoundException e) {
            logger.warn(fileNameForParse +  " FileNotFoundException");
        } catch (IOException e) {
            logger.warn("IOException");
        }

    }

    /**
     *
     * проверяет, является ли это сетевым ресурсом,
     * или локальным файлом. В зависимости от этого,
     * создается новый временный файл, в который
     * записываются данные из сети, либо просто
     * открывается локальный файл.
     * @param fileName анализируемое имя файла
     */
    @Deprecated
    public void checkFileName(String fileName) {
        if (fileName.contains("http:") || fileName.contains("https:") || fileName.contains("www")) {
            URL url = null;
            try {
                url = new URL(fileName);

                // вычитываем данные из сети, и записываем во временный файл
                InputStream is = new BufferedInputStream(url.openStream());
                File fTemp = File.createTempFile("url", ".tmp");
                fTemp.deleteOnExit();
                OutputStream os = new FileOutputStream(fTemp);
                byte[] bytes = new byte[1024];
                while(is.available() > 0) {
                    is.read(bytes);
                    os.write(bytes);
                }
                is.close();
                os.flush();
                os.close();
                fileForParse = fTemp;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            fileForParse = new File(fileName);
        }
    }


    public String getFileNameForParse() {
        return fileNameForParse;
    }

    public File getFileForParse() {
        return fileForParse;
    }

    public Lock getLock() {
        return lock;
    }

    /**
     * Генерирует ссылку на ресурс.
     * @param prefix задает тип ресурса
     * @param fileName имя ресурса
     * @return возвращает ссылку на объект типа {@code Resource}
     * @throws MalformedURLException
     */
    public Resource makeResource(String prefix, String fileName) throws MalformedURLException {
        
        Resource resource = null;

        if("-f".equals(prefix.toLowerCase().trim())){
            resource = new ResourceImplLocal(fileName);
        }
        if("-u".equals(prefix.toLowerCase().trim())){
            resource = new ResourceImplRemote(fileName);
        }
    
        return resource;
        
    }

    public Resource getResource() {
        return resource;
    }
}
