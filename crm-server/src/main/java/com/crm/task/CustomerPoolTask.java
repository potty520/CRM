package com.crm.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.entity.CrmCustomer;
import com.crm.mapper.CrmCustomerMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 公海客户回收任务
 * - 销售跟进超过30天未成交的客户自动进入公海
 * - 每天凌晨2点执行
 */
@Component
public class CustomerPoolTask {

    private final CrmCustomerMapper customerMapper;

    public CustomerPoolTask(CrmCustomerMapper customerMapper) {
        this.customerMapper = customerMapper;
    }

    /**
     * 每天凌晨2点检查公海回收
     * 将超过30天未跟进的私海客户（owner_id不为空且pool_status=0）放入公海
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void reclaimStaleCustomers() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        LambdaQueryWrapper<CrmCustomer> wrapper = new LambdaQueryWrapper<CrmCustomer>()
                .eq(CrmCustomer::getPoolStatus, 0)
                .isNotNull(CrmCustomer::getOwnerId)
                .le(CrmCustomer::getUpdateTime, threshold);
        customerMapper.selectList(wrapper).forEach(c -> {
            CrmCustomer update = new CrmCustomer();
            update.setId(c.getId());
            update.setPoolStatus(1);
            update.setOwnerId(null);
            customerMapper.updateById(update);
        });
    }
}