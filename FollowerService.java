import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;
import java.util.stream.Collectors;

public class FollowerService {

    public List<List<Integer>> findMutualFollowers(JsonNode users) {
        Map<Integer, Set<Integer>> followMap = new HashMap<>();
        for (JsonNode user : users) {
            int id = user.get("id").asInt();
            Set<Integer> follows = new HashSet<>();
            for (JsonNode f : user.get("follows")) {
                follows.add(f.asInt());
            }
            followMap.put(id, follows);
        }

        Set<String> seen = new HashSet<>();
        List<List<Integer>> result = new ArrayList<>();

        for (int u : followMap.keySet()) {
            for (int v : followMap.get(u)) {
                if (followMap.containsKey(v) && followMap.get(v).contains(u)) {
                    int a = Math.min(u, v);
                    int b = Math.max(u, v);
                    String key = a + "," + b;
                    if (!seen.contains(key)) {
                        result.add(List.of(a, b));
                        seen.add(key);
                    }
                }
            }
        }
        return result;
    }

    public List<Integer> findNthLevelFollowers(JsonNode users, int findId, int n) {
        Map<Integer, Set<Integer>> graph = new HashMap<>();
        for (JsonNode user : users) {
            int id = user.get("id").asInt();
            Set<Integer> follows = new HashSet<>();
            for (JsonNode f : user.get("follows")) {
                follows.add(f.asInt());
            }
            graph.put(id, follows);
        }

        Set<Integer> current = new HashSet<>(Collections.singleton(findId));
        Set<Integer> visited = new HashSet<>(current);

        for (int i = 0; i < n; i++) {
            Set<Integer> next = new HashSet<>();
            for (int node : current) {
                if (graph.containsKey(node)) {
                    for (int follower : graph.get(node)) {
                        if (!visited.contains(follower)) {
                            next.add(follower);
                        }
                    }
                }
            }
            visited.addAll(next);
            current = next;
        }
        return current.stream().sorted().collect(Collectors.toList());
    }
}
