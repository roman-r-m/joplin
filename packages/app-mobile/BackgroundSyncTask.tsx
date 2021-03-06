const React = require('react');
import shim from '@joplin/lib/shim';
shim.setReact(React);

import Logger, { TargetType } from '@joplin/lib/Logger';
import BaseModel from '@joplin/lib/BaseModel';
import BaseService from '@joplin/lib/services/BaseService';
import ResourceService from '@joplin/lib/services/ResourceService';
import KvStore from '@joplin/lib/services/KvStore';
import Setting from '@joplin/lib/models/Setting';
import RNFetchBlob from 'rn-fetch-blob';
import uuid from '@joplin/lib/uuid';
import { loadKeychainServiceAndSettings } from '@joplin/lib/services/SettingUtils';
import KeychainServiceDriverMobile from '@joplin/lib/services/keychain/KeychainServiceDriver.mobile';
import { setLocale } from '@joplin/lib/locale';
import SyncTargetJoplinServer from '@joplin/lib/SyncTargetJoplinServer';
import SyncTargetOneDrive from '@joplin/lib/SyncTargetOneDrive';


import NavService from '@joplin/lib/services/NavService';
const { shimInit } = require('./utils/shim-init-react.js');
import Note from '@joplin/lib/models/Note';
import Folder from '@joplin/lib/models/Folder';
const BaseSyncTarget = require('@joplin/lib/BaseSyncTarget.js');
import Resource from '@joplin/lib/models/Resource';
import Tag from '@joplin/lib/models/Tag';
import NoteTag from '@joplin/lib/models/NoteTag';
import BaseItem from '@joplin/lib/models/BaseItem';
import MasterKey from '@joplin/lib/models/MasterKey';
import Revision from '@joplin/lib/models/Revision';
import RevisionService from '@joplin/lib/services/RevisionService';
import JoplinDatabase from '@joplin/lib/JoplinDatabase';
import Database from '@joplin/lib/database';
const { DatabaseDriverReactNative } = require('./utils/database-driver-react-native');
import { reg } from '@joplin/lib/registry';
const { FileApiDriverLocal } = require('@joplin/lib/file-api-driver-local.js');
import ResourceFetcher from '@joplin/lib/services/ResourceFetcher';

const SyncTargetRegistry = require('@joplin/lib/SyncTargetRegistry.js');
const SyncTargetFilesystem = require('@joplin/lib/SyncTargetFilesystem.js');
const SyncTargetNextcloud = require('@joplin/lib/SyncTargetNextcloud.js');
const SyncTargetWebDAV = require('@joplin/lib/SyncTargetWebDAV.js');
const SyncTargetDropbox = require('@joplin/lib/SyncTargetDropbox.js');
const SyncTargetAmazonS3 = require('@joplin/lib/SyncTargetAmazonS3.js');

SyncTargetRegistry.addClass(SyncTargetOneDrive);
SyncTargetRegistry.addClass(SyncTargetNextcloud);
SyncTargetRegistry.addClass(SyncTargetWebDAV);
SyncTargetRegistry.addClass(SyncTargetDropbox);
SyncTargetRegistry.addClass(SyncTargetFilesystem);
SyncTargetRegistry.addClass(SyncTargetAmazonS3);
SyncTargetRegistry.addClass(SyncTargetJoplinServer);

import FsDriverRN from './utils/fs-driver-rn';
import DecryptionWorker from '@joplin/lib/services/DecryptionWorker';
import EncryptionService from '@joplin/lib/services/EncryptionService';
import MigrationService from '@joplin/lib/services/MigrationService';


function resourceFetcher_downloadComplete(event: any) {
	if (event.encrypted) {
		void DecryptionWorker.instance().scheduleStart();
	}
}

function decryptionWorker_resourceMetadataButNotBlobDecrypted() {
	ResourceFetcher.instance().scheduleAutoAddResources();
}

const SyncTask = async (_data: any) => {
	console.log('ZZZ Hello from JS');

	const dispatch = (command: any) => {
		console.log(`ZZZ dispatch ${JSON.stringify(command)}`);
	};

	try {
		// init
		shimInit();

		// @ts-ignore
		Setting.setConstant('env', __DEV__ ? 'dev' : 'prod');
		Setting.setConstant('appId', 'net.cozic.joplin-mobile');
		Setting.setConstant('appType', 'mobile');
		Setting.setConstant('resourceDir', RNFetchBlob.fs.dirs.DocumentDir);

		const logDatabase = new Database(new DatabaseDriverReactNative());
		await logDatabase.open({ name: 'log.sqlite' });
		await logDatabase.exec(Logger.databaseCreateTableSql());

		const mainLogger = new Logger();
		mainLogger.addTarget(TargetType.Database, { database: logDatabase, source: 'm' });
		mainLogger.setLevel(Logger.LEVEL_INFO);

		if (Setting.value('env') == 'dev') {
			mainLogger.addTarget(TargetType.Console);
			mainLogger.setLevel(Logger.LEVEL_DEBUG);
		}

		Logger.initializeGlobalLogger(mainLogger);

		reg.setLogger(mainLogger);
		reg.setShowErrorMessageBoxHandler((message: string) => { alert(message); });

		BaseService.logger_ = mainLogger;

		reg.logger().info('====================================');
		reg.logger().info(`Starting background sync service for application ${Setting.value('appId')} (${Setting.value('env')})`);

		const dbLogger = new Logger();
		dbLogger.addTarget(TargetType.Database, { database: logDatabase, source: 'm' });
		if (Setting.value('env') == 'dev') {
			dbLogger.addTarget(TargetType.Console);
			dbLogger.setLevel(Logger.LEVEL_DEBUG); // Set to LEVEL_DEBUG for full SQL queries
		} else {
			dbLogger.setLevel(Logger.LEVEL_INFO);
		}

		const db = new JoplinDatabase(new DatabaseDriverReactNative());
		db.setLogger(dbLogger);
		reg.setDb(db);

		// reg.dispatch = dispatch;
		BaseModel.dispatch = dispatch;
		// FoldersScreenUtils.dispatch = dispatch;
		BaseSyncTarget.dispatch = dispatch;
		NavService.dispatch = dispatch;
		BaseModel.setDb(db);

		KvStore.instance().setDb(reg.db());

		BaseItem.loadClass('Note', Note);
		BaseItem.loadClass('Folder', Folder);
		BaseItem.loadClass('Resource', Resource);
		BaseItem.loadClass('Tag', Tag);
		BaseItem.loadClass('NoteTag', NoteTag);
		BaseItem.loadClass('MasterKey', MasterKey);
		BaseItem.loadClass('Revision', Revision);

		const fsDriver = new FsDriverRN();

		Resource.fsDriver_ = fsDriver;
		FileApiDriverLocal.fsDriver_ = fsDriver;

		// AlarmService.setDriver(new AlarmServiceDriver(mainLogger));
		// AlarmService.setLogger(mainLogger);


		if (Setting.value('env') == 'prod') {
			await db.open({ name: 'joplin.sqlite' });
		} else {
			await db.open({ name: 'joplin-100.sqlite' });
		}

		reg.logger().info('Database is ready.');
		reg.logger().info('Loading settings...');

		await loadKeychainServiceAndSettings(KeychainServiceDriverMobile);

		if (!Setting.value('clientId')) Setting.setValue('clientId', uuid.create());

		// if (Setting.value('firstStart')) {
		// 	let locale = NativeModules.I18nManager.localeIdentifier;
		// 	if (!locale) locale = defaultLocale();
		// 	Setting.setValue('locale', closestSupportedLocale(locale));
		// 	Setting.setValue('firstStart', 0);
		// }

		// if (Setting.value('db.ftsEnabled') === -1) {
		// 	const ftsEnabled = await db.ftsEnabled();
		// 	Setting.setValue('db.ftsEnabled', ftsEnabled ? 1 : 0);
		// 	reg.logger().info('db.ftsEnabled = ', Setting.value('db.ftsEnabled'));
		// }

		// if (Setting.value('env') === 'dev') {
		// 	Setting.setValue('welcome.enabled', false);
		// }

		// PluginAssetsLoader.instance().setLogger(mainLogger);
		// await PluginAssetsLoader.instance().importAssets();

		// eslint-disable-next-line require-atomic-updates
		BaseItem.revisionService_ = RevisionService.instance();

		// Note: for now we hard-code the folder sort order as we need to
		// create a UI to allow customisation (started in branch mobile_add_sidebar_buttons)
		Setting.setValue('folders.sortOrder.field', 'title');
		Setting.setValue('folders.sortOrder.reverse', false);

		reg.logger().info(`Sync target: ${Setting.value('sync.target')}`);

		setLocale(Setting.value('locale'));

		// ----------------------------------------------------------------
		// E2EE SETUP
		// ----------------------------------------------------------------

		EncryptionService.fsDriver_ = fsDriver;
		EncryptionService.instance().setLogger(mainLogger);
		// eslint-disable-next-line require-atomic-updates
		BaseItem.encryptionService_ = EncryptionService.instance();
		DecryptionWorker.instance().dispatch = dispatch;
		DecryptionWorker.instance().setLogger(mainLogger);
		DecryptionWorker.instance().setKvStore(KvStore.instance());
		DecryptionWorker.instance().setEncryptionService(EncryptionService.instance());
		await EncryptionService.instance().loadMasterKeysFromSettings();
		DecryptionWorker.instance().on('resourceMetadataButNotBlobDecrypted', decryptionWorker_resourceMetadataButNotBlobDecrypted);

		// ----------------------------------------------------------------
		// / E2EE SETUP
		// ----------------------------------------------------------------

		reg.logger().info('Loading folders...');

		// await FoldersScreenUtils.refreshFolders();

		// const tags = await Tag.allWithNotes();

		// dispatch({
		// type: 'TAG_UPDATE_ALL',
		// items: tags,
		// });

		const masterKeys = await MasterKey.all();

		dispatch({
			type: 'MASTERKEY_UPDATE_ALL',
			items: masterKeys,
		});

		// const folderId = Setting.value('activeFolderId');
		// let folder = await Folder.load(folderId);

		// if (!folder) folder = await Folder.defaultFolder();

		// dispatch({
		// type: 'FOLDER_SET_COLLAPSED_ALL',
		// ids: Setting.value('collapsedFolderIds'),
		// });

		// if (!folder) {
		// 	dispatch(DEFAULT_ROUTE);
		// } else {
		// 	dispatch({
		// 		type: 'NAV_GO',
		// 		routeName: 'Notes',
		// 		folderId: folder.id,
		// 	});
		// }

		// setUpQuickActions(dispatch, folderId);

		reg.setupRecurrentSync();

		// PoorManIntervals.setTimeout(() => {
		// 	void AlarmService.garbageCollect();
		// }, 1000 * 60 * 60);

		ResourceService.runInBackground();

		ResourceFetcher.instance().setFileApi(() => { return reg.syncTarget().fileApi(); });
		ResourceFetcher.instance().setLogger(reg.logger());
		ResourceFetcher.instance().dispatch = dispatch;
		ResourceFetcher.instance().on('downloadComplete', resourceFetcher_downloadComplete);
		void ResourceFetcher.instance().start();

		// SearchEngine.instance().setDb(reg.db());
		// SearchEngine.instance().setLogger(reg.logger());
		// SearchEngine.instance().scheduleSyncTables();

		await MigrationService.instance().run();

		// When the app starts we want the full sync to
		// start almost immediately to get the latest data.
		// void reg.scheduleSync(1000).then(() => {
		// Wait for the first sync before updating the notifications, since synchronisation
		// might change the notifications.
		// void AlarmService.updateAllNotifications();

		// void DecryptionWorker.instance().scheduleStart();
		// });

		// await WelcomeUtils.install(dispatch);

		// Collect revisions more frequently on mobile because it doesn't auto-save
		// and it cannot collect anything when the app is not active.
		// RevisionService.instance().runInBackground(1000 * 30);

		reg.logger().info('Application initialized');

	} catch (error) {
		alert(`Initialization error: ${error.message}`);
		console.error('Initialization error:', error);
	}


	console.log('ZZZ start sync');
	await reg.scheduleSync(0);
	console.log('ZZZ end sync');

	// new Sync
	// await initialize(async (command: any) => {
	// console.log(`ZZZ dispatch: ${JSON.stringify(command)}`);
	// });
};

export default SyncTask;