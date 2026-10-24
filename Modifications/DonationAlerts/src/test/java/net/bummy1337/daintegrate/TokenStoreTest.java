package net.bummy1337.daintegrate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class TokenStoreTest {
    @Test
    void parsesBareToken() {
        Assertions.assertEquals("AbCdEf123456", TokenStore.parse("  AbCdEf123456 \n"));
    }

    @Test
    void parsesWidgetLink() {
        var link = "https://www.donationalerts.com/widget/alerts?group_id=1&token=Xy12Zw34Qq56";
        Assertions.assertEquals("Xy12Zw34Qq56", TokenStore.parse(link));
    }

    @Test
    void rejectsGarbage() {
        Assertions.assertNull(TokenStore.parse(""));
        Assertions.assertNull(TokenStore.parse("hello world"));
        Assertions.assertNull(TokenStore.parse("https://www.donationalerts.com/dashboard"));
        Assertions.assertNull(TokenStore.parse(null));
    }

    @Test
    void masksToken() {
        Assertions.assertEquals("••••••••3456", TokenStore.mask("AbCdEf123456"));
    }

    @Test
    void savesAndLoads() throws IOException {
        var file = File.createTempFile("da-token", ".txt");
        file.deleteOnExit();
        var store = new TokenStore(file);
        store.save("AbCdEf123456");
        Assertions.assertEquals("AbCdEf123456", store.load());
        store.delete();
        Assertions.assertNull(store.load());
    }
}
