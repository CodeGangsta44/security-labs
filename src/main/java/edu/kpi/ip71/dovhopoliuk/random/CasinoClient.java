package edu.kpi.ip71.dovhopoliuk.random;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.kpi.ip71.dovhopoliuk.random.model.AccountInfo;
import edu.kpi.ip71.dovhopoliuk.random.model.BetInfo;
import edu.kpi.ip71.dovhopoliuk.random.model.ErrorInfo;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.Objects;

public class CasinoClient {

    private static final String CASINO_URL = Config.getProperty("casino.url");
    private final ObjectMapper mapper;

    public CasinoClient() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    public AccountInfo createAccount(int id) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(CASINO_URL + "/createacc" + "?id=" + id);

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                StatusLine status = response.getStatusLine();
                int statusCode = status.getStatusCode();
                HttpEntity entity = response.getEntity();
                if (statusCode == 201) {
                    if (Objects.nonNull(entity)) {
                        return mapper.readValue(entity.getContent(), AccountInfo.class);
                    } else {
                        throw new NullPointerException();
                    }
                } else {
                    if (Objects.nonNull(entity)) {
                        ErrorInfo errorInfo = mapper.readValue(entity.getContent(), ErrorInfo.class);
                        throw new CasinoException(errorInfo.getError());
                    } else {
                        throw new NullPointerException();
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public BetInfo makeBet(PlayMode mode, int playerId, int amountOfMoney, int betNumber) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String uri =
                    CASINO_URL + "/play" + mode + "?id=" + playerId + "&bet=" + amountOfMoney + "&number=" + betNumber;
            HttpGet httpGet = new HttpGet(uri);

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                StatusLine status = response.getStatusLine();
                int statusCode = status.getStatusCode();
                HttpEntity entity = response.getEntity();
                if (statusCode == 200) {
                    if (Objects.nonNull(entity)) {
                        return mapper.readValue(entity.getContent(), BetInfo.class);
                    } else {
                        throw new NullPointerException();
                    }
                } else {
                    if (Objects.nonNull(entity)) {
                        ErrorInfo errorInfo = mapper.readValue(entity.getContent(), ErrorInfo.class);
                        throw new CasinoException(errorInfo.getError());
                    } else {
                        throw new NullPointerException();
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
