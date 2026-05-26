package com.crm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.entity.CrmCustomer;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CrmCustomerMapper extends BaseMapper<CrmCustomer> {
}
