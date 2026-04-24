package com.sustbbgz.virtualspringbootbackend.service;

import com.sustbbgz.virtualspringbootbackend.entity.SceneAsset;
import com.sustbbgz.virtualspringbootbackend.mapper.SceneAssetMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SceneAssetServiceTest {

    @Mock
    private SceneAssetMapper sceneAssetMapper;

    @Mock
    private CosService cosService;

    @Mock
    private ResourceUrlService resourceUrlService;

    @InjectMocks
    private SceneAssetService sceneAssetService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sceneAssetService, "storageType", "local");
    }

    @Test
    void shouldStoreSceneMetadataInTextureInfoField() {
        String metadata = "{\"version\":\"1.0.0\",\"textures\":{},\"bindings\":[]}";

        sceneAssetService.updateTextureInfo(10L, metadata);

        verify(sceneAssetMapper).updateTextureInfo(10L, metadata);
    }
}
