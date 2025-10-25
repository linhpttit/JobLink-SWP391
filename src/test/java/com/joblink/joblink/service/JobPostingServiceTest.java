package com.joblink.joblink.service;

import com.joblink.joblink.Repository.*;
import com.joblink.joblink.dto.JobPostingDto;
import com.joblink.joblink.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JobPostingServiceTest {

    @Mock
    private JobPostingRepository jobPostingRepository;
    @Mock
    private SkillRepository skillRepository;
    @Mock
    private ProvinceRepository provinceRepository;
    @Mock
    private DistrictRepository districtRepository;
    @Mock
    private EmployerRepository employerRepository;

    @InjectMocks
    private JobPostingService jobPostingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ✅ TEST 1: createJobPosting()
    @Test
    void testCreateJobPosting_Success() {
        JobPostingDto dto = new JobPostingDto();
        dto.setTitle("Java Dev");
        dto.setSkillId(1L);
        dto.setProvinceId(1L);
        dto.setDistrictId(2L);

        Skill skill = new Skill(); skill.setSkillId(1L);
        Province province = new Province(); province.setProvinceId(1L);
        District district = new District(); district.setDistrictId(2L);

        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill));
        when(provinceRepository.findById(1L)).thenReturn(Optional.of(province));
        when(districtRepository.findById(2L)).thenReturn(Optional.of(district));
        when(jobPostingRepository.save(any(JobPosting.class))).thenAnswer(i -> i.getArgument(0));

        JobPosting result = jobPostingService.createJobPosting(dto);

        assertEquals("Java Dev", result.getTitle());
        assertEquals(skill, result.getSkill());
        assertEquals(province, result.getProvince());
        verify(jobPostingRepository, times(1)).save(any(JobPosting.class));
    }

    // ✅ TEST 2: updateJobPosting()
    @Test
    void testUpdateJobPosting_Success() {
        JobPosting existing = new JobPosting();
        existing.setJobId(1L);
        JobPostingDto dto = new JobPostingDto();
        dto.setTitle("Updated Job");
        dto.setSkillId(1L);
        dto.setProvinceId(2L);
        dto.setDistrictId(3L);

        when(jobPostingRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(skillRepository.findById(1L)).thenReturn(Optional.of(new Skill()));
        when(provinceRepository.findById(2L)).thenReturn(Optional.of(new Province()));
        when(districtRepository.findById(3L)).thenReturn(Optional.of(new District()));
        when(jobPostingRepository.save(any(JobPosting.class))).thenAnswer(i -> i.getArgument(0));

        Optional<JobPosting> result = jobPostingService.updateJobPosting(1L, dto);
        assertTrue(result.isPresent());
        assertEquals("Updated Job", result.get().getTitle());
    }

    // ✅ TEST 3: deleteJobPostingById()
    @Test
    void testDeleteJobPostingById() {
        jobPostingService.deleteJobPostingById(1L);
        verify(jobPostingRepository, times(1)).deleteById(1L);
    }

    // ✅ TEST 4: editJobPostingByEntity()
    @Test
    void testEditJobPostingByEntity_Success() {
        JobPosting existing = new JobPosting();
        existing.setJobId(1L);
        when(jobPostingRepository.findById(1L)).thenReturn(Optional.of(existing));

        JobPosting updated = new JobPosting();
        updated.setTitle("New Title");

        jobPostingService.editJobPostingByEntity(1L, updated);
        verify(jobPostingRepository).save(existing);
        assertEquals("New Title", existing.getTitle());
    }

    // ✅ TEST 5: getAllJobPostings()
    @Test
    void testGetAllJobPostings() {
        List<JobPosting> postings = List.of(new JobPosting(), new JobPosting());
        when(jobPostingRepository.findAll()).thenReturn(postings);

        List<JobPosting> result = jobPostingService.getAllJobPostings();
        assertEquals(2, result.size());
    }

    // ✅ TEST 6: findJobPostingById()
    @Test
    void testFindJobPostingById() {
        JobPosting job = new JobPosting();
        when(jobPostingRepository.findById(1L)).thenReturn(Optional.of(job));

        Optional<JobPosting> result = jobPostingService.findJobPostingById(1L);
        assertTrue(result.isPresent());
    }

    // ✅ TEST 7: findJobPostingsByEmployer()
    @Test
    void testFindJobPostingsByEmployer() {
        List<JobPosting> postings = List.of(new JobPosting());
        when(jobPostingRepository.findByEmployerId(10L)).thenReturn(postings);

        List<JobPosting> result = jobPostingService.findJobPostingsByEmployer(10L);
        assertEquals(1, result.size());
        verify(jobPostingRepository).findByEmployerId(10L);
    }

    // ✅ TEST 8: changeJobPostingStatus()
    @Test
    void testChangeJobPostingStatus_Success() {
        JobPosting posting = new JobPosting();
        posting.setJobId(5L);
        when(jobPostingRepository.findById(5L)).thenReturn(Optional.of(posting));
        when(jobPostingRepository.save(any(JobPosting.class))).thenAnswer(i -> i.getArgument(0));

        Optional<JobPosting> result = jobPostingService.changeJobPostingStatus(5L, "ACTIVE");
        assertTrue(result.isPresent());
        assertEquals("ACTIVE", result.get().getStatus());
    }

    // ✅ TEST 9: getRelatedJobs()
    @Test
    void testGetRelatedJobs_ReturnsEmptyList() {
        List<JobPosting> result = jobPostingService.getRelatedJobs(1, 10L);
        assertTrue(result.isEmpty());
    }

    // ✅ TEST 10: countOpenJobPosting()
    @Test
    void testCountOpenJobPosting() {
        when(jobPostingRepository.countByStatus("ACTIVE")).thenReturn(3L);
        long result = jobPostingService.countOpenJobPosting();
        assertEquals(3L, result);
    }
}
