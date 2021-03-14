import { Platform, NativeModules } from 'react-native';

console.log('ZZZ load picker');
let isAvailable = false;
if (Platform.OS === 'android') {
	NativeModules.DirectoryPicker.isAvailable().then((res: boolean) => {
		console.log(`ZZZ isAvailable ${res}`);
		isAvailable = res;
	});
}

export default {

	isAvailable: (): boolean => isAvailable,

	async pick(): Promise<string> {
		return NativeModules.DirectoryPicker.pick();
	},

};
