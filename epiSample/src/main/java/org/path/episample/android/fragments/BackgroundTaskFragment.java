/*
 * Copyright (C) 2012-2013 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.path.episample.android.fragments;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.path.common.android.listener.LicenseReaderListener;
import org.path.episample.android.R;
import org.path.episample.android.listeners.DeleteFormsListener;
import org.path.episample.android.listeners.FormDownloaderListener;
import org.path.episample.android.listeners.FormListDownloaderListener;
import org.path.episample.android.listeners.InitializationListener;
import org.path.episample.android.listeners.InstanceUploaderListener;
import org.path.episample.android.listeners.JoinRestoreListener;
import org.path.episample.android.listeners.MarkCensusAsInvalidListener;
import org.path.episample.android.listeners.PointSelectionListener;
import org.path.episample.android.listeners.ReceiveCensusDataListener;
import org.path.episample.android.listeners.RemoveAllCensusListener;
import org.path.episample.android.listeners.RemoveCensusByDateListener;
import org.path.episample.android.listeners.RemoveCensusDataFromPreviousDateListener;
import org.path.episample.android.listeners.RestoreListener;
import org.path.episample.android.listeners.SendCensusDataListener;
import org.path.episample.android.logic.FormDetails;
import org.path.episample.android.logic.InstanceUploadOutcome;
import org.path.episample.android.logic.PairSearch.Pair;
import org.path.episample.android.tasks.DeleteFormsTask;
import org.path.episample.android.tasks.DownloadFormListTask;
import org.path.episample.android.tasks.DownloadFormsTask;
import org.path.episample.android.tasks.InitializationTask;
import org.path.episample.android.tasks.InstanceUploaderTask;
import org.path.episample.android.tasks.JoinRestoreTask;
import org.path.episample.android.tasks.LicenseReaderTask;
import org.path.episample.android.tasks.MarkCensusAsInvalidTask;
import org.path.episample.android.tasks.ReceiveCensusDataTask;
import org.path.episample.android.tasks.RemoveAllCensusDataTask;
import org.path.episample.android.tasks.RemoveCensusByDateTask;
import org.path.episample.android.tasks.RemoveCensusDataFromPreviousDateTask;
import org.path.episample.android.tasks.RestoreTask;
import org.path.episample.android.tasks.SelectPointsTask;
import org.path.episample.android.tasks.SendCensusDataTask;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * Wrapper that holds all the background tasks that might be in-progress at any
 * time.
 * 
 * @author mitchellsundt@gmail.com
 * 
 */
public class BackgroundTaskFragment extends Fragment implements
		LicenseReaderListener, DeleteFormsListener, FormListDownloaderListener,
		FormDownloaderListener, InstanceUploaderListener,
		InitializationListener, PointSelectionListener, SendCensusDataListener,
		ReceiveCensusDataListener, JoinRestoreListener, RestoreListener,
		RemoveCensusByDateListener, RemoveAllCensusListener,
		RemoveCensusDataFromPreviousDateListener, MarkCensusAsInvalidListener {

	public static final class BackgroundTasks {
		LicenseReaderTask mLicenseReaderTask = null;
		DeleteFormsTask mDeleteFormsTask = null;
		DownloadFormListTask mDownloadFormListTask = null;
		DownloadFormsTask mDownloadFormsTask = null;
		InstanceUploaderTask mInstanceUploaderTask = null;
		InitializationTask mInitializationTask = null;

		SelectPointsTask mSelectPointsTask = null;
		SendCensusDataTask mSendCensusDataTask = null;
		ReceiveCensusDataTask mReceiveCensusDataTask = null;
		JoinRestoreTask mJoinRestoreTask = null;
		RestoreTask mRestoreTask = null;
		RemoveCensusByDateTask mRemoveCensusByDateTask = null;
		RemoveAllCensusDataTask mRemoveAllCensusTask = null;
		MarkCensusAsInvalidTask mMarkCensusAsInvalidTask = null;
		RemoveCensusDataFromPreviousDateTask mRemoveCensusDataFromPreviousDateTask = null;

		BackgroundTasks() {
		};
	}

	public BackgroundTasks mBackgroundTasks; // handed across orientation
	// changes

	public LicenseReaderListener mLicenseReaderListener = null;
	public DeleteFormsListener mDeleteFormsListener = null;
	public FormListDownloaderListener mFormListDownloaderListener = null;
	public FormDownloaderListener mFormDownloaderListener = null;
	public InstanceUploaderListener mInstanceUploaderListener = null;
	public InitializationListener mInitializationListener = null;

	public PointSelectionListener mPointSelectionListener = null;
	public SendCensusDataListener mSendCensusDataListener = null;
	public ReceiveCensusDataListener mReceiveCensusDataListener = null;
	public JoinRestoreListener mJoinRestoreListener = null;
	public RestoreListener mRestoreListener = null;
	public RemoveCensusByDateListener mRemoveCensusByDateListener = null;
	public RemoveAllCensusListener mRemoveAllCensusListener = null;
	public MarkCensusAsInvalidListener mMarkCensusAsInvalidListener = null;
	public RemoveCensusDataFromPreviousDateListener mRemoveCensusDataFromPreviousDateListener = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBackgroundTasks = new BackgroundTasks();

		setRetainInstance(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setRetainInstance(true);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return new View(getActivity());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private <T> void executeTask(AsyncTask<T, ?, ?> task, T... args) {

		int androidVersion = android.os.Build.VERSION.SDK_INT;
		if (androidVersion < 11) {
			task.execute(args);
		} else {
			// TODO: execute on serial executor in version 11 onward...
			task.execute(args);
			// task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, (Void[]) null);
		}

	}

	@Override
	public void onStart() {
		super.onStart();
		// run the disk sync task once...
		// startDiskSyncListener(((ODKActivity) getActivity()).getAppName(),
		// null);
	}

	@Override
	public void onPause() {
		mLicenseReaderListener = null;
		mDeleteFormsListener = null;
		mFormListDownloaderListener = null;
		mFormDownloaderListener = null;
		mInstanceUploaderListener = null;
		mInitializationListener = null;

		mPointSelectionListener = null;
		mSendCensusDataListener = null;
		mJoinRestoreListener = null;
		mRestoreListener = null;
		mRemoveCensusByDateListener = null;
		mRemoveAllCensusListener = null;
		mMarkCensusAsInvalidListener = null;
		mRemoveCensusDataFromPreviousDateListener = null;

		if (mBackgroundTasks.mLicenseReaderTask != null) {
			mBackgroundTasks.mLicenseReaderTask.setLicenseReaderListener(null);
		}
		if (mBackgroundTasks.mDeleteFormsTask != null) {
			mBackgroundTasks.mDeleteFormsTask.setDeleteListener(null);
		}
		if (mBackgroundTasks.mDownloadFormListTask != null) {
			mBackgroundTasks.mDownloadFormListTask.setDownloaderListener(null);
		}
		if (mBackgroundTasks.mDownloadFormsTask != null) {
			mBackgroundTasks.mDownloadFormsTask.setDownloaderListener(null);
		}
		if (mBackgroundTasks.mInstanceUploaderTask != null) {
			mBackgroundTasks.mInstanceUploaderTask.setUploaderListener(null);
		}
		if (mBackgroundTasks.mInitializationTask != null) {
			mBackgroundTasks.mInitializationTask
					.setInitializationListener(null);
		}

		if (mBackgroundTasks.mSelectPointsTask != null) {
			mBackgroundTasks.mSelectPointsTask.setPointSelectionListener(null);
		}

		if (mBackgroundTasks.mSendCensusDataTask != null) {
			mBackgroundTasks.mSendCensusDataTask
					.setSendCensusDataListener(null);
		}

		if (mBackgroundTasks.mReceiveCensusDataTask != null) {
			mBackgroundTasks.mReceiveCensusDataTask
					.setReceiveCensusDataListener(null);
		}

		if (mBackgroundTasks.mJoinRestoreTask != null) {
			mBackgroundTasks.mJoinRestoreTask.setJoinRestoreListener(null);
		}

		if (mBackgroundTasks.mRestoreTask != null) {
			mBackgroundTasks.mRestoreTask.setRestoreListener(null);
		}

		if (mBackgroundTasks.mRemoveCensusByDateTask != null) {
			mBackgroundTasks.mRemoveCensusByDateTask
					.setRemoveCensusByDateListener(null);
		}

		if (mBackgroundTasks.mRemoveAllCensusTask != null) {
			mBackgroundTasks.mRemoveAllCensusTask
					.setRemoveAllCensusListener(null);
		}

		if (mBackgroundTasks.mMarkCensusAsInvalidTask != null) {
			mBackgroundTasks.mMarkCensusAsInvalidTask
					.setMarkCensusAsInvalidListener(null);
		}

		if (mBackgroundTasks.mRemoveCensusDataFromPreviousDateTask != null) {
			mBackgroundTasks.mRemoveCensusDataFromPreviousDateTask
					.setRemoveCensusDataFromPreviousDateListener(null);
		}

		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mBackgroundTasks.mLicenseReaderTask != null) {
			mBackgroundTasks.mLicenseReaderTask.setLicenseReaderListener(this);
		}
		if (mBackgroundTasks.mDeleteFormsTask != null) {
			mBackgroundTasks.mDeleteFormsTask.setDeleteListener(this);
		}
		if (mBackgroundTasks.mDownloadFormListTask != null) {
			mBackgroundTasks.mDownloadFormListTask.setDownloaderListener(this);
		}
		if (mBackgroundTasks.mDownloadFormsTask != null) {
			mBackgroundTasks.mDownloadFormsTask.setDownloaderListener(this);
		}
		if (mBackgroundTasks.mInstanceUploaderTask != null) {
			mBackgroundTasks.mInstanceUploaderTask.setUploaderListener(this);
		}
		if (mBackgroundTasks.mInitializationTask != null) {
			mBackgroundTasks.mInitializationTask
					.setInitializationListener(this);
		}

		if (mBackgroundTasks.mSelectPointsTask != null) {
			mBackgroundTasks.mSelectPointsTask.setPointSelectionListener(this);
		}

		if (mBackgroundTasks.mSendCensusDataTask != null) {
			mBackgroundTasks.mSendCensusDataTask
					.setSendCensusDataListener(this);
		}

		if (mBackgroundTasks.mReceiveCensusDataTask != null) {
			mBackgroundTasks.mReceiveCensusDataTask
					.setReceiveCensusDataListener(this);
		}

		if (mBackgroundTasks.mJoinRestoreTask != null) {
			mBackgroundTasks.mJoinRestoreTask.setJoinRestoreListener(this);
		}

		if (mBackgroundTasks.mRestoreTask != null) {
			mBackgroundTasks.mRestoreTask.setRestoreListener(this);
		}

		if (mBackgroundTasks.mRemoveCensusByDateTask != null) {
			mBackgroundTasks.mRemoveCensusByDateTask
					.setRemoveCensusByDateListener(this);
		}

		if (mBackgroundTasks.mRemoveAllCensusTask != null) {
			mBackgroundTasks.mRemoveAllCensusTask
					.setRemoveAllCensusListener(this);
		}

		if (mBackgroundTasks.mMarkCensusAsInvalidTask != null) {
			mBackgroundTasks.mMarkCensusAsInvalidTask
					.setMarkCensusAsInvalidListener(this);
		}

		if (mBackgroundTasks.mRemoveCensusDataFromPreviousDateTask != null) {
			mBackgroundTasks.mRemoveCensusDataFromPreviousDateTask
					.setRemoveCensusDataFromPreviousDateListener(this);
		}
	}

	// /////////////////////////////////////////////////////////////////////////
	// registrations

	public void establishReadLicenseListener(LicenseReaderListener listener) {
		mLicenseReaderListener = listener;
		// async task may have completed while we were reorienting...
		if (mBackgroundTasks.mLicenseReaderTask != null
				&& mBackgroundTasks.mLicenseReaderTask.getStatus() == AsyncTask.Status.FINISHED) {
			this.readLicenseComplete(mBackgroundTasks.mLicenseReaderTask
					.getResult());
		}
	}

	public void establishDeleteFormsListener(DeleteFormsListener listener) {
		mDeleteFormsListener = listener;
		// async task may have completed while we were reorienting...
		if (mBackgroundTasks.mDeleteFormsTask != null
				&& mBackgroundTasks.mDeleteFormsTask.getStatus() == AsyncTask.Status.FINISHED) {
			this.deleteFormsComplete(
					mBackgroundTasks.mDeleteFormsTask.getDeleteCount(),
					mBackgroundTasks.mDeleteFormsTask.getDeleteFormData());
		}
	}

	public void establishFormListDownloaderListener(
			FormListDownloaderListener listener) {
		mFormListDownloaderListener = listener;
		// async task may have completed while we were reorienting...
		if (mBackgroundTasks.mDownloadFormListTask != null
				&& mBackgroundTasks.mDownloadFormListTask.getStatus() == AsyncTask.Status.FINISHED) {
			this.formListDownloadingComplete(mBackgroundTasks.mDownloadFormListTask
					.getFormList());
		}
	}

	public void establishFormDownloaderListener(FormDownloaderListener listener) {
		mFormDownloaderListener = listener;
		// async task may have completed while we were reorienting...
		if (mBackgroundTasks.mDownloadFormsTask != null
				&& mBackgroundTasks.mDownloadFormsTask.getStatus() == AsyncTask.Status.FINISHED) {
			this.formsDownloadingComplete(mBackgroundTasks.mDownloadFormsTask
					.getResult());
		}
	}

	public void establishInstanceUploaderListener(
			InstanceUploaderListener listener) {
		mInstanceUploaderListener = listener;
		// async task may have completed while we were reorienting...
		if (mBackgroundTasks.mInstanceUploaderTask != null
				&& mBackgroundTasks.mInstanceUploaderTask.getStatus() == AsyncTask.Status.FINISHED) {
			this.uploadingComplete(mBackgroundTasks.mInstanceUploaderTask
					.getResult());
		}
	}

	public void establishInitializationListener(InitializationListener listener) {
		mInitializationListener = listener;
		// async task may have completed while we were reorienting...
		if (mBackgroundTasks.mInitializationTask != null
				&& mBackgroundTasks.mInitializationTask.getStatus() == AsyncTask.Status.FINISHED) {
			this.initializationComplete(
					mBackgroundTasks.mInitializationTask.getOverallSuccess(),
					mBackgroundTasks.mInitializationTask.getResult());
		}
	}

	// belendia
	public void establishPointSelectionListener(PointSelectionListener listener) {
		mPointSelectionListener = listener;
		// async task may have completed while we were reorienting...
		if (mBackgroundTasks.mSelectPointsTask != null
				&& mBackgroundTasks.mSelectPointsTask.getStatus() == AsyncTask.Status.FINISHED) {
			this.selectionComplete(mBackgroundTasks.mSelectPointsTask
					.getSuccess());
		}
	}

	public void establishSendCensusDataListener(SendCensusDataListener listener) {
		mSendCensusDataListener = listener;
		// async task may have completed while we were reorienting...
		if (mBackgroundTasks.mSendCensusDataTask != null
				&& mBackgroundTasks.mSendCensusDataTask.getStatus() == AsyncTask.Status.FINISHED) {
			this.sendingComplete(mBackgroundTasks.mSendCensusDataTask
					.getTotalSent());
		}
	}

	public void establishReceiveCensusDataListener(
			ReceiveCensusDataListener listener) {
		mReceiveCensusDataListener = listener;
		// async task may have completed while we were reorienting...
		if (mBackgroundTasks.mReceiveCensusDataTask != null
				&& mBackgroundTasks.mReceiveCensusDataTask.getStatus() == AsyncTask.Status.FINISHED) {
			this.receivingComplete(mBackgroundTasks.mReceiveCensusDataTask
					.getTotalSent());
		}
	}

	public void establishJoinRestoreListener(JoinRestoreListener listener) {
		mJoinRestoreListener = listener;
		// async task may have completed while we were reorienting...
		if (mBackgroundTasks.mJoinRestoreTask != null
				&& mBackgroundTasks.mJoinRestoreTask.getStatus() == AsyncTask.Status.FINISHED) {
			this.joinRestoreComplete(mBackgroundTasks.mJoinRestoreTask
					.getSuccess());
		}
	}

	public void establishRestoreListener(RestoreListener listener) {
		mRestoreListener = listener;
		// async task may have completed while we were reorienting...
		if (mBackgroundTasks.mRestoreTask != null
				&& mBackgroundTasks.mRestoreTask.getStatus() == AsyncTask.Status.FINISHED) {
			this.restoreComplete(mBackgroundTasks.mRestoreTask.getSuccess());
		}
	}

	public void establishRemoveCensusByDateListener(
			RemoveCensusByDateListener listener) {
		mRemoveCensusByDateListener = listener;
		// async task may have completed while we were reorienting...
		if (mBackgroundTasks.mRemoveCensusByDateTask != null
				&& mBackgroundTasks.mRemoveCensusByDateTask.getStatus() == AsyncTask.Status.FINISHED) {
			this.removeCensusByDateComplete(
					mBackgroundTasks.mRemoveCensusByDateTask.getSuccess(),
					mBackgroundTasks.mRemoveCensusByDateTask
							.getNumberOfRowsAffected());
		}
	}

	public void establishRemoveAllCensusListener(
			RemoveAllCensusListener listener) {
		mRemoveAllCensusListener = listener;
		// async task may have completed while we were reorienting...
		if (mBackgroundTasks.mRemoveAllCensusTask != null
				&& mBackgroundTasks.mRemoveAllCensusTask.getStatus() == AsyncTask.Status.FINISHED) {
			this.removeAllCensusComplete(mBackgroundTasks.mRemoveAllCensusTask
					.getSuccess(), mBackgroundTasks.mRemoveAllCensusTask
					.getNumOfAffectedRows());
		}
	}

	public void establishMarkCensusAsInvalidListener(
			MarkCensusAsInvalidListener listener) {
		mMarkCensusAsInvalidListener = listener;
		// async task may have completed while we were reorienting...
		if (mBackgroundTasks.mMarkCensusAsInvalidTask != null
				&& mBackgroundTasks.mMarkCensusAsInvalidTask.getStatus() == AsyncTask.Status.FINISHED) {
			this.markAsInvalidComplete(mBackgroundTasks.mMarkCensusAsInvalidTask
					.getPairs());
		}
	}

	public void establishRemoveCensus4PreviousDateListener(
			RemoveCensusDataFromPreviousDateListener listener) {
		mRemoveCensusDataFromPreviousDateListener = listener;
		// async task may have completed while we were reorienting...
		if (mBackgroundTasks.mRemoveCensusDataFromPreviousDateTask != null
				&& mBackgroundTasks.mRemoveCensusDataFromPreviousDateTask
						.getStatus() == AsyncTask.Status.FINISHED) {
			this.removeCensusFromPreviousDateComplete(
					mBackgroundTasks.mRemoveCensusDataFromPreviousDateTask
							.getSuccess(),
					mBackgroundTasks.mRemoveCensusDataFromPreviousDateTask
							.getNumOfAffectedRows());
		}
	}

	// ///////////////////////////////////////////////////
	// actions

	public synchronized void readLicenseFile(String appName,
			LicenseReaderListener listener) {
		mLicenseReaderListener = listener;
		if (mBackgroundTasks.mLicenseReaderTask != null
				&& mBackgroundTasks.mLicenseReaderTask.getStatus() != AsyncTask.Status.FINISHED) {
			Toast.makeText(this.getActivity(),
					getString(R.string.still_reading_license_file),
					Toast.LENGTH_LONG).show();
		} else {
			LicenseReaderTask lrt = new LicenseReaderTask();
			lrt.setApplication(getActivity().getApplication());
			lrt.setAppName(appName);
			lrt.setLicenseReaderListener(this);
			mBackgroundTasks.mLicenseReaderTask = lrt;
			executeTask(mBackgroundTasks.mLicenseReaderTask);
		}
	}

	public synchronized void deleteSelectedForms(String appName,
			DeleteFormsListener listener, String[] toDelete,
			boolean deleteFormAndData) {
		mDeleteFormsListener = listener;
		if (mBackgroundTasks.mDeleteFormsTask != null
				&& mBackgroundTasks.mDeleteFormsTask.getStatus() != AsyncTask.Status.FINISHED) {
			Toast.makeText(this.getActivity(),
					getString(R.string.file_delete_in_progress),
					Toast.LENGTH_LONG).show();
		} else {
			DeleteFormsTask df = new DeleteFormsTask();
			df.setApplication(getActivity().getApplication());
			df.setAppName(appName);
			df.setDeleteListener(this);
			df.setDeleteFormData(deleteFormAndData);
			mBackgroundTasks.mDeleteFormsTask = df;
			executeTask(mBackgroundTasks.mDeleteFormsTask, toDelete);
		}
	}

	public synchronized void downloadFormList(String appName,
			FormListDownloaderListener listener) {
		mFormListDownloaderListener = listener;
		if (mBackgroundTasks.mDownloadFormListTask != null
				&& mBackgroundTasks.mDownloadFormListTask.getStatus() != AsyncTask.Status.FINISHED) {
			Toast.makeText(this.getActivity(),
					getString(R.string.download_in_progress), Toast.LENGTH_LONG)
					.show();
		} else {
			DownloadFormListTask df = new DownloadFormListTask(appName);
			df.setApplication(getActivity().getApplication());
			df.setDownloaderListener(this);
			mBackgroundTasks.mDownloadFormListTask = df;
			executeTask(mBackgroundTasks.mDownloadFormListTask, (Void[]) null);
		}
	}

	public synchronized void downloadForms(String appName,
			FormDownloaderListener listener, FormDetails[] filesToDownload) {
		mFormDownloaderListener = listener;
		if (mBackgroundTasks.mDownloadFormsTask != null
				&& mBackgroundTasks.mDownloadFormsTask.getStatus() != AsyncTask.Status.FINISHED) {
			Toast.makeText(this.getActivity(),
					getString(R.string.download_in_progress), Toast.LENGTH_LONG)
					.show();
		} else {
			DownloadFormsTask df = new DownloadFormsTask();
			df.setApplication(getActivity().getApplication());
			df.setAppName(appName);
			df.setDownloaderListener(this);
			mBackgroundTasks.mDownloadFormsTask = df;
			executeTask(mBackgroundTasks.mDownloadFormsTask, filesToDownload);
		}
	}

	public synchronized void uploadInstances(InstanceUploaderListener listener,
			String appName, String uploadTableId, String[] instancesToUpload) {
		mInstanceUploaderListener = listener;
		if (mBackgroundTasks.mInstanceUploaderTask != null
				&& mBackgroundTasks.mInstanceUploaderTask.getStatus() != AsyncTask.Status.FINISHED) {
			Toast.makeText(this.getActivity(),
					getString(R.string.upload_in_progress), Toast.LENGTH_LONG)
					.show();
		} else {
			InstanceUploaderTask iu = new InstanceUploaderTask(appName,
					uploadTableId);
			iu.setApplication(getActivity().getApplication());
			iu.setUploaderListener(this);
			mBackgroundTasks.mInstanceUploaderTask = iu;
			executeTask(mBackgroundTasks.mInstanceUploaderTask,
					instancesToUpload);
		}
	}

	public synchronized void initializeAppName(String appName,
			InitializationListener listener) {
		mInitializationListener = listener;
		if (mBackgroundTasks.mInitializationTask != null
				&& mBackgroundTasks.mInitializationTask.getStatus() != AsyncTask.Status.FINISHED) {
			// Toast.makeText(this.getActivity(),
			// getString(R.string.expansion_in_progress),
			// Toast.LENGTH_LONG).show();
		} else {
			InitializationTask cf = new InitializationTask();
			cf.setApplication(getActivity().getApplication());
			cf.setAppName(appName);
			cf.setInitializationListener(this);
			mBackgroundTasks.mInitializationTask = cf;
			executeTask(mBackgroundTasks.mInitializationTask, (Void) null);
		}
	}

	public synchronized void selectPoints(String appName,
			PointSelectionListener listener, int mainPoints,
			int additionalPoints, int alternatePoints) {
		mPointSelectionListener = listener;
		if (mBackgroundTasks.mSelectPointsTask != null
				&& mBackgroundTasks.mSelectPointsTask.getStatus() != AsyncTask.Status.FINISHED) {
			Toast.makeText(this.getActivity(),
					getString(R.string.selection_in_progress),
					Toast.LENGTH_LONG).show();
		} else {
			SelectPointsTask sp = new SelectPointsTask();
			sp.setApplication(getActivity().getApplication());
			sp.setAppName(appName);
			sp.setPointSelectionListener(this);
			mBackgroundTasks.mSelectPointsTask = sp;
			executeTask(mBackgroundTasks.mSelectPointsTask, mainPoints,
					additionalPoints, alternatePoints);

		}
	}

	public synchronized void sendCensusData(SendCensusDataListener listener,
			InetAddress inetAddress) {
		mSendCensusDataListener = listener;
		if (mBackgroundTasks.mSendCensusDataTask != null
				&& mBackgroundTasks.mSendCensusDataTask.getStatus() != AsyncTask.Status.FINISHED) {
			Toast.makeText(this.getActivity(),
					getString(R.string.sending_census_in_progress),
					Toast.LENGTH_LONG).show();
		} else {
			SendCensusDataTask sp = new SendCensusDataTask();
			sp.setSendCensusDataListener(this);
			mBackgroundTasks.mSendCensusDataTask = sp;
			executeTask(mBackgroundTasks.mSendCensusDataTask, inetAddress);

		}
	}

	public synchronized void receiveCensusData(
			ReceiveCensusDataListener listener, InetAddress inetAddress) {
		mReceiveCensusDataListener = listener;
		if (mBackgroundTasks.mReceiveCensusDataTask != null
				&& mBackgroundTasks.mReceiveCensusDataTask.getStatus() != AsyncTask.Status.FINISHED) {
			Toast.makeText(this.getActivity(),
					getString(R.string.receiving_census_in_progress),
					Toast.LENGTH_LONG).show();
		} else {
			ReceiveCensusDataTask sp = new ReceiveCensusDataTask();
			sp.setReceiveCensusDataListener(this);
			mBackgroundTasks.mReceiveCensusDataTask = sp;
			executeTask(mBackgroundTasks.mReceiveCensusDataTask, inetAddress);

		}
	}

	public synchronized void joinRestore(JoinRestoreListener listener,
			String path) {
		mJoinRestoreListener = listener;
		if (mBackgroundTasks.mJoinRestoreTask != null
				&& mBackgroundTasks.mJoinRestoreTask.getStatus() != AsyncTask.Status.FINISHED) {
			Toast.makeText(this.getActivity(),
					getString(R.string.restoring_in_progress),
					Toast.LENGTH_LONG).show();
		} else {
			JoinRestoreTask sp = new JoinRestoreTask();
			sp.setJoinRestoreListener(this);
			mBackgroundTasks.mJoinRestoreTask = sp;
			executeTask(mBackgroundTasks.mJoinRestoreTask, path);

		}
	}

	public synchronized void restore(RestoreListener listener, String path) {
		mRestoreListener = listener;
		if (mBackgroundTasks.mRestoreTask != null
				&& mBackgroundTasks.mRestoreTask.getStatus() != AsyncTask.Status.FINISHED) {
			Toast.makeText(this.getActivity(),
					getString(R.string.restoring_in_progress),
					Toast.LENGTH_LONG).show();
		} else {
			RestoreTask sp = new RestoreTask();
			sp.setRestoreListener(this);
			mBackgroundTasks.mRestoreTask = sp;
			executeTask(mBackgroundTasks.mRestoreTask, path);

		}
	}

	public synchronized void removeCensusByDate(
			RemoveCensusByDateListener listener, ArrayList<String> dates) {
		mRemoveCensusByDateListener = listener;
		if (mBackgroundTasks.mRemoveCensusByDateTask != null
				&& mBackgroundTasks.mRemoveCensusByDateTask.getStatus() != AsyncTask.Status.FINISHED) {
			Toast.makeText(this.getActivity(),
					getString(R.string.removing_in_progress), Toast.LENGTH_LONG)
					.show();
		} else {
			RemoveCensusByDateTask sp = new RemoveCensusByDateTask();
			sp.setRemoveCensusByDateListener(this);
			mBackgroundTasks.mRemoveCensusByDateTask = sp;
			executeTask(mBackgroundTasks.mRemoveCensusByDateTask, dates);

		}
	}

	public synchronized void removeAllCensus(RemoveAllCensusListener listener) {
		mRemoveAllCensusListener = listener;
		if (mBackgroundTasks.mRemoveAllCensusTask != null
				&& mBackgroundTasks.mRemoveAllCensusTask.getStatus() != AsyncTask.Status.FINISHED) {
			Toast.makeText(this.getActivity(),
					getString(R.string.removing_in_progress), Toast.LENGTH_LONG)
					.show();
		} else {
			RemoveAllCensusDataTask sp = new RemoveAllCensusDataTask();
			sp.setRemoveAllCensusListener(this);
			mBackgroundTasks.mRemoveAllCensusTask = sp;
			executeTask(mBackgroundTasks.mRemoveAllCensusTask);

		}
	}

	public synchronized void markCensusAsInvalid(
			MarkCensusAsInvalidListener listener, int meters, int distanceType) {
		mMarkCensusAsInvalidListener = listener;
		if (mBackgroundTasks.mMarkCensusAsInvalidTask != null
				&& mBackgroundTasks.mMarkCensusAsInvalidTask.getStatus() != AsyncTask.Status.FINISHED) {
			Toast.makeText(this.getActivity(),
					getString(R.string.invalidating_in_progress),
					Toast.LENGTH_LONG).show();
		} else {
			MarkCensusAsInvalidTask sp = new MarkCensusAsInvalidTask();
			sp.setMarkCensusAsInvalidListener(this);
			mBackgroundTasks.mMarkCensusAsInvalidTask = sp;
			executeTask(mBackgroundTasks.mMarkCensusAsInvalidTask, meters,
					distanceType);

		}
	}

	public synchronized void removeCensusFromPreviousDate(
			RemoveCensusDataFromPreviousDateListener listener) {
		mRemoveCensusDataFromPreviousDateListener = listener;
		if (mBackgroundTasks.mRemoveCensusDataFromPreviousDateTask != null
				&& mBackgroundTasks.mRemoveCensusDataFromPreviousDateTask
						.getStatus() != AsyncTask.Status.FINISHED) {
			Toast.makeText(this.getActivity(),
					getString(R.string.removing_in_progress), Toast.LENGTH_LONG)
					.show();
		} else {
			RemoveCensusDataFromPreviousDateTask sp = new RemoveCensusDataFromPreviousDateTask();
			sp.setRemoveCensusDataFromPreviousDateListener(this);
			mBackgroundTasks.mRemoveCensusDataFromPreviousDateTask = sp;
			executeTask(mBackgroundTasks.mRemoveCensusDataFromPreviousDateTask);

		}
	}

	// /////////////////////////////////////////////////////////////////////////
	// clearing tasks
	//
	// NOTE: clearing these makes us forget that they are running, but it is
	// up to the task itself to eventually shutdown. i.e., we don't quite
	// know when they actually stop.

	public synchronized void clearDownloadFormListTask() {
		mFormListDownloaderListener = null;
		if (mBackgroundTasks.mDownloadFormListTask != null) {
			mBackgroundTasks.mDownloadFormListTask.setDownloaderListener(null);
			if (mBackgroundTasks.mDownloadFormListTask.getStatus() != AsyncTask.Status.FINISHED) {
				mBackgroundTasks.mDownloadFormListTask.cancel(true);
			}
		}
		mBackgroundTasks.mDownloadFormListTask = null;
	}

	public synchronized void clearDownloadFormsTask() {
		mFormDownloaderListener = null;
		if (mBackgroundTasks.mDownloadFormsTask != null) {
			mBackgroundTasks.mDownloadFormsTask.setDownloaderListener(null);
			if (mBackgroundTasks.mDownloadFormsTask.getStatus() != AsyncTask.Status.FINISHED) {
				mBackgroundTasks.mDownloadFormsTask.cancel(true);
			}
		}
		mBackgroundTasks.mDownloadFormsTask = null;
	}

	public synchronized void clearUploadInstancesTask() {
		mInstanceUploaderListener = null;
		if (mBackgroundTasks.mInstanceUploaderTask != null) {
			mBackgroundTasks.mInstanceUploaderTask.setUploaderListener(null);
			if (mBackgroundTasks.mInstanceUploaderTask.getStatus() != AsyncTask.Status.FINISHED) {
				mBackgroundTasks.mInstanceUploaderTask.cancel(true);
			}
		}
		mBackgroundTasks.mInstanceUploaderTask = null;
	}

	public synchronized void clearInitializationTask() {
		mInitializationListener = null;
		if (mBackgroundTasks.mInitializationTask != null) {
			mBackgroundTasks.mInitializationTask
					.setInitializationListener(null);
			if (mBackgroundTasks.mInitializationTask.getStatus() != AsyncTask.Status.FINISHED) {
				mBackgroundTasks.mInitializationTask.cancel(true);
			}
		}
		mBackgroundTasks.mInitializationTask = null;
	}

	public synchronized void clearSelectPointsTask() {
		mPointSelectionListener = null;
		if (mBackgroundTasks.mSelectPointsTask != null) {
			mBackgroundTasks.mSelectPointsTask.setPointSelectionListener(null);
			if (mBackgroundTasks.mSelectPointsTask.getStatus() != AsyncTask.Status.FINISHED) {
				mBackgroundTasks.mSelectPointsTask.cancel(true);
			}
		}
		mBackgroundTasks.mSelectPointsTask = null;
	}

	public synchronized void clearSendCensusDataTask() {
		mSendCensusDataListener = null;
		if (mBackgroundTasks.mSendCensusDataTask != null) {
			mBackgroundTasks.mSendCensusDataTask
					.setSendCensusDataListener(null);
			if (mBackgroundTasks.mSendCensusDataTask.getStatus() != AsyncTask.Status.FINISHED) {
				mBackgroundTasks.mSendCensusDataTask.cancel(true);
			}
		}
		mBackgroundTasks.mSendCensusDataTask = null;
	}

	public synchronized void clearReceiveCensusDataTask() {
		mReceiveCensusDataListener = null;
		if (mBackgroundTasks.mReceiveCensusDataTask != null) {
			mBackgroundTasks.mReceiveCensusDataTask
					.setReceiveCensusDataListener(null);
			if (mBackgroundTasks.mReceiveCensusDataTask.getStatus() != AsyncTask.Status.FINISHED) {
				mBackgroundTasks.mReceiveCensusDataTask.cancel(true);
			}
		}
		mBackgroundTasks.mReceiveCensusDataTask = null;
	}

	public synchronized void clearJoinRestoreTask() {
		mJoinRestoreListener = null;
		if (mBackgroundTasks.mJoinRestoreTask != null) {
			mBackgroundTasks.mJoinRestoreTask.setJoinRestoreListener(null);
			if (mBackgroundTasks.mJoinRestoreTask.getStatus() != AsyncTask.Status.FINISHED) {
				mBackgroundTasks.mJoinRestoreTask.cancel(true);
			}
		}
		mBackgroundTasks.mJoinRestoreTask = null;
	}

	public synchronized void clearRestoreTask() {
		mRestoreListener = null;
		if (mBackgroundTasks.mRestoreTask != null) {
			mBackgroundTasks.mRestoreTask.setRestoreListener(null);
			if (mBackgroundTasks.mRestoreTask.getStatus() != AsyncTask.Status.FINISHED) {
				mBackgroundTasks.mRestoreTask.cancel(true);
			}
		}
		mBackgroundTasks.mRestoreTask = null;
	}

	public synchronized void clearRemoveCensusByDateTask() {
		mRemoveCensusByDateListener = null;
		if (mBackgroundTasks.mRemoveCensusByDateTask != null) {
			mBackgroundTasks.mRemoveCensusByDateTask
					.setRemoveCensusByDateListener(null);
			if (mBackgroundTasks.mRemoveCensusByDateTask.getStatus() != AsyncTask.Status.FINISHED) {
				mBackgroundTasks.mRemoveCensusByDateTask.cancel(true);
			}
		}
		mBackgroundTasks.mRemoveCensusByDateTask = null;
	}

	public synchronized void clearRemoveAllCensusTask() {
		mRemoveAllCensusListener = null;
		if (mBackgroundTasks.mRemoveAllCensusTask != null) {
			mBackgroundTasks.mRemoveAllCensusTask
					.setRemoveAllCensusListener(null);
			if (mBackgroundTasks.mRemoveAllCensusTask.getStatus() != AsyncTask.Status.FINISHED) {
				mBackgroundTasks.mRemoveAllCensusTask.cancel(true);
			}
		}
		mBackgroundTasks.mRemoveAllCensusTask = null;
	}

	public synchronized void clearMarkCensusAsInvalidTask() {
		mMarkCensusAsInvalidListener = null;
		if (mBackgroundTasks.mMarkCensusAsInvalidTask != null) {
			mBackgroundTasks.mMarkCensusAsInvalidTask
					.setMarkCensusAsInvalidListener(null);
			if (mBackgroundTasks.mMarkCensusAsInvalidTask.getStatus() != AsyncTask.Status.FINISHED) {
				mBackgroundTasks.mMarkCensusAsInvalidTask.cancel(true);
			}
		}
		mBackgroundTasks.mMarkCensusAsInvalidTask = null;
	}

	public synchronized void clearRemoveCensusDataFromPreviousDateTask() {
		mRemoveCensusDataFromPreviousDateListener = null;
		if (mBackgroundTasks.mRemoveCensusDataFromPreviousDateTask != null) {
			mBackgroundTasks.mRemoveCensusDataFromPreviousDateTask
					.setRemoveCensusDataFromPreviousDateListener(null);
			if (mBackgroundTasks.mRemoveCensusDataFromPreviousDateTask
					.getStatus() != AsyncTask.Status.FINISHED) {
				mBackgroundTasks.mRemoveCensusDataFromPreviousDateTask
						.cancel(true);
			}
		}
		mBackgroundTasks.mRemoveCensusDataFromPreviousDateTask = null;
	}

	// /////////////////////////////////////////////////////////////////////////
	// cancel requests
	//
	// These maintain communications paths, so that we get a failure
	// completion callback eventually.

	public synchronized void cancelDownloadFormListTask() {
		if (mBackgroundTasks.mDownloadFormListTask != null) {
			if (mBackgroundTasks.mDownloadFormListTask.getStatus() != AsyncTask.Status.FINISHED) {
				mBackgroundTasks.mDownloadFormListTask.cancel(true);
			}
		}
	}

	public synchronized void cancelDownloadFormsTask() {
		if (mBackgroundTasks.mDownloadFormsTask != null) {
			if (mBackgroundTasks.mDownloadFormsTask.getStatus() != AsyncTask.Status.FINISHED) {
				mBackgroundTasks.mDownloadFormsTask.cancel(true);
			}
		}
	}

	public synchronized void cancelUploadInstancesTask() {
		if (mBackgroundTasks.mInstanceUploaderTask != null) {
			if (mBackgroundTasks.mInstanceUploaderTask.getStatus() != AsyncTask.Status.FINISHED) {
				mBackgroundTasks.mInstanceUploaderTask.cancel(true);
			}
		}
	}

	public synchronized void cancelInitializationTask() {
		if (mBackgroundTasks.mInitializationTask != null) {
			if (mBackgroundTasks.mInitializationTask.getStatus() != AsyncTask.Status.FINISHED) {
				mBackgroundTasks.mInitializationTask.cancel(true);
			}
		}
	}

	public synchronized void cancelSelectPointsTask() {
		if (mBackgroundTasks.mSelectPointsTask != null) {
			if (mBackgroundTasks.mSelectPointsTask.getStatus() != AsyncTask.Status.FINISHED) {
				mBackgroundTasks.mSelectPointsTask.cancel(true);
			}
		}
	}

	public synchronized void cancelSendCensusDataTask() {
		if (mBackgroundTasks.mSendCensusDataTask != null) {
			if (mBackgroundTasks.mSendCensusDataTask.getStatus() != AsyncTask.Status.FINISHED) {
				mBackgroundTasks.mSendCensusDataTask.cancel(true);
			}
		}
	}

	public synchronized void cancelReceiveCensusDataTask() {
		if (mBackgroundTasks.mReceiveCensusDataTask != null) {
			if (mBackgroundTasks.mReceiveCensusDataTask.getStatus() != AsyncTask.Status.FINISHED) {
				mBackgroundTasks.mReceiveCensusDataTask.cancel(true);
			}
		}
	}

	public synchronized void cancelJoinRestoreTask() {
		if (mBackgroundTasks.mJoinRestoreTask != null) {
			if (mBackgroundTasks.mJoinRestoreTask.getStatus() != AsyncTask.Status.FINISHED) {
				mBackgroundTasks.mJoinRestoreTask.cancel(true);
			}
		}
	}

	public synchronized void cancelRestoreTask() {
		if (mBackgroundTasks.mRestoreTask != null) {
			if (mBackgroundTasks.mRestoreTask.getStatus() != AsyncTask.Status.FINISHED) {
				mBackgroundTasks.mRestoreTask.cancel(true);
			}
		}
	}

	public synchronized void cancelRemoveCensusByDateTask() {
		if (mBackgroundTasks.mRemoveCensusByDateTask != null) {
			if (mBackgroundTasks.mRemoveCensusByDateTask.getStatus() != AsyncTask.Status.FINISHED) {
				mBackgroundTasks.mRemoveCensusByDateTask.cancel(true);
			}
		}
	}

	public synchronized void cancelRemoveAllCensusTask() {
		if (mBackgroundTasks.mRemoveAllCensusTask != null) {
			if (mBackgroundTasks.mRemoveAllCensusTask.getStatus() != AsyncTask.Status.FINISHED) {
				mBackgroundTasks.mRemoveAllCensusTask.cancel(true);
			}
		}
	}

	public synchronized void cancelMarkCensusAsInvalidTask() {
		if (mBackgroundTasks.mMarkCensusAsInvalidTask != null) {
			if (mBackgroundTasks.mMarkCensusAsInvalidTask.getStatus() != AsyncTask.Status.FINISHED) {
				mBackgroundTasks.mMarkCensusAsInvalidTask.cancel(true);
			}
		}
	}

	public synchronized void cancelRemoveCensusDataFromPreviousDateTask() {
		if (mBackgroundTasks.mRemoveCensusDataFromPreviousDateTask != null) {
			if (mBackgroundTasks.mRemoveCensusDataFromPreviousDateTask
					.getStatus() != AsyncTask.Status.FINISHED) {
				mBackgroundTasks.mRemoveCensusDataFromPreviousDateTask
						.cancel(true);
			}
		}
	}

	// /////////////////////////////////////////////////////////////////////////
	// callbacks

	@Override
	public void readLicenseComplete(String result) {
		if (mLicenseReaderListener != null) {
			mLicenseReaderListener.readLicenseComplete(result);
		}
		mBackgroundTasks.mLicenseReaderTask = null;
	}

	@Override
	public void deleteFormsComplete(int deletedForms, boolean deleteFormData) {
		if (mDeleteFormsListener != null) {
			mDeleteFormsListener.deleteFormsComplete(deletedForms,
					deleteFormData);
		}
		mBackgroundTasks.mDeleteFormsTask = null;
	}

	@Override
	public void formListDownloadingComplete(HashMap<String, FormDetails> value) {
		if (mFormListDownloaderListener != null) {
			mFormListDownloaderListener.formListDownloadingComplete(value);
		}
	}

	@Override
	public void formsDownloadingComplete(HashMap<String, String> result) {
		if (mFormDownloaderListener != null) {
			mFormDownloaderListener.formsDownloadingComplete(result);
		}
	}

	@Override
	public void formDownloadProgressUpdate(String currentFile, int progress,
			int total) {
		if (mFormDownloaderListener != null) {
			mFormDownloaderListener.formDownloadProgressUpdate(currentFile,
					progress, total);
		}
	}

	@Override
	public void uploadingComplete(InstanceUploadOutcome result) {
		if (mInstanceUploaderListener != null) {
			mInstanceUploaderListener.uploadingComplete(result);
		}
	}

	@Override
	public void progressUpdate(int progress, int total) {
		if (mInstanceUploaderListener != null) {
			mInstanceUploaderListener.progressUpdate(progress, total);
		}
	}

	@Override
	public void initializationComplete(boolean overallSuccess,
			ArrayList<String> result) {
		if (mInitializationListener != null) {
			mInitializationListener.initializationComplete(overallSuccess,
					result);
		}
	}

	@Override
	public void initializationProgressUpdate(String status) {
		if (mInitializationListener != null) {
			mInitializationListener.initializationProgressUpdate(status);
		}
	}

	@Override
	public void selectionComplete(boolean success) {
		if (mPointSelectionListener != null) {
			mPointSelectionListener.selectionComplete(success);
		}
	}

	@Override
	public void selectionCanceled() {
		if (mPointSelectionListener != null) {
			mPointSelectionListener.selectionCanceled();
		}
	}

	@Override
	public void sendingComplete(int totalSent) {
		if (mSendCensusDataListener != null) {
			mSendCensusDataListener.sendingComplete(totalSent);
		}
	}

	@Override
	public void sendProgressUpdate(String message) {
		if (mSendCensusDataListener != null) {
			mSendCensusDataListener.sendProgressUpdate(message);
		}

	}

	@Override
	public void sendingCanceled(int totalSent) {
		if (mSendCensusDataListener != null) {
			mSendCensusDataListener.sendingCanceled(totalSent);
		}
	}

	@Override
	public void sendError(int totalSent, String message) {
		if (mSendCensusDataListener != null) {
			mSendCensusDataListener.sendError(totalSent, message);
		}
	}

	@Override
	public void receivingComplete(int totalSent) {
		if (mReceiveCensusDataListener != null) {
			mReceiveCensusDataListener.receivingComplete(totalSent);
		}
	}

	@Override
	public void receiveProgressUpdate(String message) {
		if (mReceiveCensusDataListener != null) {
			mReceiveCensusDataListener.receiveProgressUpdate(message);
		}
	}

	@Override
	public void receivingCanceled(int totalReceived) {
		if (mReceiveCensusDataListener != null) {
			mReceiveCensusDataListener.receivingCanceled(totalReceived);
		}
	}

	@Override
	public void receiveError(int totalReceived) {
		if (mReceiveCensusDataListener != null) {
			mReceiveCensusDataListener.receiveError(totalReceived);
		}
	}

	@Override
	public void joinRestoreComplete(boolean success) {
		if (mJoinRestoreListener != null) {
			mJoinRestoreListener.joinRestoreComplete(success);
		}
	}

	@Override
	public void joinRestoreCanceled() {
		if (mJoinRestoreListener != null) {
			mJoinRestoreListener.joinRestoreCanceled();
		}
	}

	@Override
	public void joinRestoreProgressUpdate(String message) {
		if (mJoinRestoreListener != null) {
			mJoinRestoreListener.joinRestoreProgressUpdate(message);
		}
	}

	@Override
	public void restoreComplete(boolean success) {
		if (mRestoreListener != null) {
			mRestoreListener.restoreComplete(success);
		}
	}

	@Override
	public void restoreCanceled() {
		if (mRestoreListener != null) {
			mRestoreListener.restoreCanceled();
		}
	}

	@Override
	public void restoreProgressUpdate(String message) {
		if (mRestoreListener != null) {
			mRestoreListener.restoreProgressUpdate(message);
		}
	}

	@Override
	public void removeCensusByDateComplete(boolean success,
			long numOfRowsAffected) {
		if (mRemoveCensusByDateListener != null) {
			mRemoveCensusByDateListener.removeCensusByDateComplete(success,
					numOfRowsAffected);
		}
	}

	@Override
	public void removeCensusByDateCanceled() {
		if (mRemoveCensusByDateListener != null) {
			mRemoveCensusByDateListener.removeCensusByDateCanceled();
		}
	}

	@Override
	public void removeCensusByDateProgressUpdate(String message) {
		if (mRemoveCensusByDateListener != null) {
			mRemoveCensusByDateListener
					.removeCensusByDateProgressUpdate(message);
		}
	}

	@Override
	public void removeAllCensusComplete(boolean success, long numOfRowsAffected) {
		if (mRemoveAllCensusListener != null) {
			mRemoveAllCensusListener.removeAllCensusComplete(success,
					numOfRowsAffected);
		}
	}

	@Override
	public void removeAllCensusCanceled() {
		if (mRemoveAllCensusListener != null) {
			mRemoveAllCensusListener.removeAllCensusCanceled();
		}
	}

	@Override
	public void removeAllCensusProgressUpdate(String message) {
		if (mRemoveAllCensusListener != null) {
			mRemoveAllCensusListener.removeAllCensusProgressUpdate(message);
		}
	}

	@Override
	public void markAsInvalidComplete(List<Pair> pairs) {
		if (mMarkCensusAsInvalidListener != null) {
			mMarkCensusAsInvalidListener.markAsInvalidComplete(pairs);
		}
	}

	@Override
	public void markAsInvalidCanceled() {
		if (mMarkCensusAsInvalidListener != null) {
			mMarkCensusAsInvalidListener.markAsInvalidCanceled();
		}
	}

	@Override
	public void markAsInvalidProgressUpdate(String message) {
		if (mMarkCensusAsInvalidListener != null) {
			mMarkCensusAsInvalidListener.markAsInvalidProgressUpdate(message);
		}
	}

	@Override
	public void removeCensusFromPreviousDateComplete(boolean success,
			long numOfRowsAffected) {
		if (mRemoveCensusDataFromPreviousDateListener != null) {
			mRemoveCensusDataFromPreviousDateListener
					.removeCensusFromPreviousDateComplete(success,
							numOfRowsAffected);
		}
	}

	@Override
	public void removeCensusFromPreviousDateCanceled() {
		if (mRemoveCensusDataFromPreviousDateListener != null) {
			mRemoveCensusDataFromPreviousDateListener
					.removeCensusFromPreviousDateCanceled();
		}
	}

	@Override
	public void removeCensusFromPreviousDateProgressUpdate(String message) {
		if (mRemoveCensusDataFromPreviousDateListener != null) {
			mRemoveCensusDataFromPreviousDateListener
					.removeCensusFromPreviousDateProgressUpdate(message);
		}
	}
}