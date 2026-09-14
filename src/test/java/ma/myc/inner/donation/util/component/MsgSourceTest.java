package ma.myc.inner.donation.util.component;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

@ExtendWith(MockitoExtension.class)
class MsgSourceTest {

    @Mock
    private MessageSource source;

    @InjectMocks
    private MsgSource msgSource;


    @Test
    @DisplayName("Should Return Message from message.properties file")
    void Should_Return_Message_From_Properties() {
        //Given
        String[] params = {"message.param.value"};
        when(source.getMessage("message.key", params, "!message.key!", Locale.FRANCE)).thenReturn("Message Value with param");

        //When
        try (MockedStatic<LocaleContextHolder> mocked = Mockito.mockStatic(LocaleContextHolder.class)) {
            mocked.when(LocaleContextHolder::getLocale).thenReturn(Locale.FRANCE);
            String response = msgSource.getMessage("message.key", params);

            //Then
            assertThat(response).isEqualTo("Message Value with param");
        }
    }


    @Test
    @DisplayName("Should Return key when key's message does not exist in message.properties file")
    void Should_Return_Key_When_Message_Does_Not_Exist_In_Properties() {
        //Given
        String[] params = {"message.param.value"};
        when(source.getMessage("message.key", params, "!message.key!", Locale.FRANCE)).thenReturn("!message.key!");

        //When
        try (MockedStatic<LocaleContextHolder> mocked = Mockito.mockStatic(LocaleContextHolder.class)) {
            mocked.when(LocaleContextHolder::getLocale).thenReturn(Locale.FRANCE);
            String response = msgSource.getMessage("message.key", params);

            //Then
            assertThat(response).isEqualTo("!message.key!");
        }
    }

}
