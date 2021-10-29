package top.hackchen.lexer;

import org.junit.Test;
import top.hackchen.lexer.util.CompressionUtils;

/**
 * @author 陈浩天
 * @date 2021/10/17 12:41 星期日
 */
public class CompressionUtilsTest {

    @Test
    public void integersToBytes() {

    }

    @Test
    public void bytesToIntegers() {
        int[] a = CompressionUtils.compress(new int[]{1, 2, 0, 0, 0, 0, 0, 0, 0, 3, 4, 5});
        int[] b = CompressionUtils.decompress(a);
    }
}