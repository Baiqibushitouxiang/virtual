package com.sustbbgz.virtualspringbootbackend.controller;

import com.sustbbgz.virtualspringbootbackend.entity.SceneAsset;
import com.sustbbgz.virtualspringbootbackend.service.SceneAssetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SceneAssetControllerTest {

    @Mock
    private SceneAssetService sceneAssetService;

    @InjectMocks
    private SceneAssetController sceneAssetController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sceneAssetController).build();
    }

    @Test
    void shouldUpdateTextureInfoAsSceneMetadata() throws Exception {
        mockMvc.perform(post("/api/scenes/9/textures")
                        .contentType(APPLICATION_JSON)
                        .content("{\"version\":\"1.0.0\",\"textures\":{},\"bindings\":[]}"))
                .andExpect(status().isOk());

        verify(sceneAssetService).updateTextureInfo(9L, "{\"version\":\"1.0.0\",\"textures\":{},\"bindings\":[]}");
    }

    @Test
    void shouldListScenes() throws Exception {
        SceneAsset asset = new SceneAsset();
        asset.setId(1L);
        asset.setName("scene-1");
        when(sceneAssetService.list()).thenReturn(Collections.singletonList(asset));

        mockMvc.perform(get("/api/scenes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void shouldGetSceneById() throws Exception {
        SceneAsset asset = new SceneAsset();
        asset.setId(2L);
        asset.setName("scene-2");
        when(sceneAssetService.get(2L)).thenReturn(asset);

        mockMvc.perform(get("/api/scenes/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void shouldDeleteScene() throws Exception {
        mockMvc.perform(delete("/api/scenes/3"))
                .andExpect(status().isNoContent());

        verify(sceneAssetService).delete(3L);
    }

    @Test
    void shouldUploadScene() throws Exception {
        SceneAsset asset = new SceneAsset();
        asset.setId(4L);
        asset.setName("scene-upload");
        asset.setFileType("json");
        when(sceneAssetService.upload(any(), eq("scene-upload"), eq("desc"), eq(null))).thenReturn(asset);

        MockMultipartFile file = new MockMultipartFile("file", "scene.json", "application/json", "{\"name\":\"scene\"}".getBytes());
        mockMvc.perform(multipart("/api/scenes/upload")
                        .file(file)
                        .param("name", "scene-upload")
                        .param("description", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.fileType").value("json"));
    }
}
