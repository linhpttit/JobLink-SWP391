package com.joblink.joblink.service;

import com.joblink.joblink.dao.JobBookmarkDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JobBookmarkService {
	private final JobBookmarkDao jobBookmarkDao;

	public boolean isBookmarked(int seekerId, long jobId) {
		return jobBookmarkDao.exists(seekerId, jobId);
	}

	@Transactional
	public boolean addBookmark(int seekerId, long jobId) {
		if (jobBookmarkDao.exists(seekerId, jobId)) return true;
		jobBookmarkDao.add(seekerId, jobId);
		return true;
	}

	@Transactional
	public boolean removeBookmark(int seekerId, long jobId) {
		jobBookmarkDao.remove(seekerId, jobId);
		return true;
	}

	public List<Map<String, Object>> getBookmarkedJobs(int seekerId) {
		return jobBookmarkDao.getBookmarkedJobsWithDetails(seekerId);
	}

	public int countBookmarks(int seekerId) {
		return jobBookmarkDao.countBookmarksBySeeker(seekerId);
	}
}


