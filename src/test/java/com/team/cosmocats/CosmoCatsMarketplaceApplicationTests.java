package com.team.cosmocats;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CosmoCatsMarketplaceApplicationTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void completesProductLifecycleThroughTheApplication() throws Exception {
        var created = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Cosmic tuna\",\"price\":12.50,\"categoryId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.category.name").value("Food"))
                .andReturn().getResponse();
        JsonNode body = jsonMapper.readTree(created.getContentAsString());
        var location = "/api/products/" + body.get("id").asLong();

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cosmic tuna"));
        mockMvc.perform(put(location).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Moon ball\",\"price\":4.25,\"categoryId\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Moon ball"))
                .andExpect(jsonPath("$.category.name").value("Toys"));
        mockMvc.perform(get("/api/products").param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(body.get("id").asLong()))
                .andExpect(jsonPath("$.totalElements").value(1));
        mockMvc.perform(delete(location))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        mockMvc.perform(get(location))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete(location))
                .andExpect(status().isNoContent());
    }

}
