package com.joblink.joblink.service;

import com.joblink.joblink.dao.JobBookmarkDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}


