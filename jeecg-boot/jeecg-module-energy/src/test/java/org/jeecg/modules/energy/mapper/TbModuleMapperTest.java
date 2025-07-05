package org.jeecg.modules.energy.mapper;

import org.jeecg.modules.energy.entity.TbModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TbModuleMapper单元测试
 * 注意：这个测试需要连接到实际的数据库，因此需要在测试环境中配置好数据库连接
 */
@SpringBootTest
@Transactional
public class TbModuleMapperTest {

    @Autowired
    private TbModuleMapper tbModuleMapper;

    /**
     * 测试根据组织编码查询关联的仪表
     */
    @Test
    public void testSelectModulesByOrgCode() {
        // 使用已知存在的组织编码
        String orgCode = "A02A02A01"; // 注塑部门
        
        List<TbModule> modules = tbModuleMapper.selectModulesByOrgCode(orgCode);
        
        // 验证结果
        assertNotNull(modules);
        // 至少应该有一条记录
        assertFalse(modules.isEmpty());
        
        // 验证返回的数据是否符合预期
        for (TbModule module : modules) {
            assertNotNull(module.getId());
            assertNotNull(module.getModuleName());
            assertNotNull(module.getModuleId());
            assertEquals("Y", module.getIsaction()); // 只返回启用的仪表
            
            // 验证sys_org_code字段是否包含查询的orgCode
            String sysOrgCode = module.getSysOrgCode();
            assertNotNull(sysOrgCode);
            assertTrue(sysOrgCode.contains(orgCode));
        }
    }
    
    /**
     * 测试不存在的组织编码
     */
    @Test
    public void testSelectModulesByNonExistentOrgCode() {
        // 使用一个不存在的组织编码
        String orgCode = "NON_EXISTENT_ORG_CODE";
        
        List<TbModule> modules = tbModuleMapper.selectModulesByOrgCode(orgCode);
        
        // 验证结果
        assertNotNull(modules);
        // 应该没有记录
        assertTrue(modules.isEmpty());
    }
} 