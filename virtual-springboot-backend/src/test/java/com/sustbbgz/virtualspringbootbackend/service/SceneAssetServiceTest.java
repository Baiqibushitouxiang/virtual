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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SceneAssetServiceTest {

    @Mock
    private SceneAssetMapper sceneAssetMapper;

    @Mock
    private CosService cosService;

    @Mock
    private ResourceUrlService resourceUrlService;

    @Mock
    private DataPanelService dataPanelService;

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

    @Test
    void shouldDeletePanelsWhenSceneDeleted() throws Exception {
        SceneAsset asset = new SceneAsset();
        asset.setId(9L);
        asset.setPath("scenes/example.glb");
        when(sceneAssetMapper.getById(9L)).thenReturn(asset);

        sceneAssetService.delete(9L);

        verify(dataPanelService).deleteBySceneId(9L);
    }
}
