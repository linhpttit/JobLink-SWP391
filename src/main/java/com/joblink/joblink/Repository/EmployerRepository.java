//package com.joblink.joblink.Repository;
//
//import com.joblink.joblink.auth.model.EmployerProfile;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public interface EmployerRepository extends JpaRepository<EmployerProfile, Long> {
//
//    @Query(value = "EXEC sp_Employers_SearchOpen :keyword, :location, :industry, :sortBy", nativeQuery = true)
//    List<Object[]> findEmployersWithOpenJobs(
//            @Param("keyword") String keyword,
//            @Param("location") String location,
//            @Param("industry") String industry,
//            @Param("sortBy") String sortBy
//    );
//}
