import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class WebhookService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final FollowerService followerService = new FollowerService();

    private final String REG_NO = "REG12347";

    public void process() throws Exception {
        String initUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook";

        Map<String, String> requestBody = Map.of(
                "name", "John Doe",
                "regNo", REG_NO,
                "email", "john@example.com"
        );

        ResponseEntity<String> response = restTemplate.postForEntity(initUrl, requestBody, String.class);
        JsonNode json = objectMapper.readTree(response.getBody());
        String webhookUrl = json.get("webhook").asText();
        String accessToken = json.get("accessToken").asText();
        JsonNode dataNode = json.get("data");

        List<List<Integer>> outcome;
        int lastDigit = Integer.parseInt(REG_NO.replaceAll("\\D", "")) % 10;

        if (lastDigit % 2 == 1) {
            outcome = followerService.findMutualFollowers(dataNode.get("users"));
        } else {
            int n = dataNode.get("n").asInt();
            int findId = dataNode.get("findId").asInt();
            outcome = List.of(followerService.findNthLevelFollowers(dataNode.get("users"), findId, n));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("regNo", REG_NO);
        result.put("outcome", outcome);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", accessToken);

        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(result), headers);

        int retries = 0;
        while (retries < 4) {
            try {
                ResponseEntity<String> postResponse = restTemplate.postForEntity(webhookUrl, entity, String.class);
                if (postResponse.getStatusCode().is2xxSuccessful()) {
                    break;
                }
            } catch (Exception e) {
                retries++;
                Thread.sleep(1000);
            }
        }
    }
}
