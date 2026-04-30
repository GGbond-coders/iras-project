package com.iras.mapper;

import com.iras.entity.JobInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface JobMapper {

    /**
     * 多字段模糊搜索 + 分页
     */
    List<JobInfo> searchJobs(@Param("jobName") String jobName,
                             @Param("city") String city,
                             @Param("salaryMin") String salaryMin,
                             @Param("salaryMax") String salaryMax,
                             @Param("offset") int offset,
                             @Param("size") int size);

    /**
     * 统计搜索结果总数
     */
    long countSearchJobs(@Param("jobName") String jobName,
                         @Param("city") String city,
                         @Param("salaryMin") String salaryMin,
                         @Param("salaryMax") String salaryMax);

    @Select("SELECT * FROM job_info WHERE id = #{id}")
    JobInfo findById(Long id);

    /**
     * 获取所有职位（供 Dify 诊断匹配用）
     */
    @Select("SELECT * FROM job_info")
    List<JobInfo> findAll();
}
