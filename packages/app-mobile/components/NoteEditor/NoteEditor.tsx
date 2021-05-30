const { _ } = require('@joplin/lib/locale');
import { useCallback, useImperativeHandle, useRef, useState } from 'react';
import { Text, NativeSyntheticEvent, TextInputSelectionChangeEventData, TouchableOpacity, View } from 'react-native';
const { Platform, TextInput } = require('react-native');
const React = require('react');

interface Selection {
    start: number;
    end: number;
}
interface Props {
    ref: any;
    style: any;
    defaultValue: string;
    initialSelection: Selection;
    onChangeText: (text: string)=> Promise<void>;
    onSelectionChange: (e: NativeSyntheticEvent<TextInputSelectionChangeEventData>)=> void;
    theme: any;
}

export default React.forwardRef((props: Props, ref: any) => {

	const inputRef = useRef(null);

	useImperativeHandle(ref, () => {
		return {
			insertAtSelection: async (insert: string) => {
				console.log(`insert at selection: ${JSON.stringify(selection)}`);
				if (selection) {
					const start = selection.start;
					const end = selection.end;
					const newText = text.substring(0, start) + insert + text.substring(end);
					await setText2(newText);
				} else {
					await setText2(text + insert);
				}
			},
		};
	});

	const [text, setText] = useState<string>(props.defaultValue);
	const setText2 = async (text: string) => {
		setText(text);
		await props.onChangeText(text);
	};

	const onChangeText = useCallback(async (text: string) => {
		await setText2(text);
	}, [props.onChangeText]);

	const [selection, setSelection] = useState<Selection>(props.initialSelection);
	const onSelectionChange = useCallback((e: NativeSyntheticEvent<TextInputSelectionChangeEventData>)=> {
		console.log(`on selection change ${JSON.stringify(e.nativeEvent.selection)}`);
		if (selection && !e.nativeEvent.selection) return;

		setSelection(e.nativeEvent.selection);
		props.onSelectionChange(e);
	}, [props.onSelectionChange]);

	const wrapSelectionWith = async (delimiter: string) => {
		if (!selection) return;

		const start = selection.start;
		const end = selection.end;
		if (start == end) {
			await setText2(text.substring(0, start) + delimiter + text.substring(start));
		} else {
			await setText2(text.substring(0, start) + delimiter + text.substring(start, end) + delimiter + text.substring(end));
		}
	};

	const style = Object.assign({}, props.style, { flex: 1 });

	return (
		<View style={{ display: 'flex', flex: 1, flexDirection: 'column' }}>
			<TextInput
				enableMarkdown={true}
				ref={inputRef}
				autoCapitalize="sentences"
				style={style}
				multiline={true}
				defaultValue={props.defaultValue}
				value={text}
				onChangeText={onChangeText}
				onSelectionChange={onSelectionChange}
				blurOnSubmit={false}
				selectionColor={props.theme.textSelectionColor}
				keyboardAppearance={props.theme.keyboardAppearance}
				placeholder={_('Add body')}
				placeholderTextColor={props.theme.colorFaded}
				// need some extra padding for iOS so that the keyboard won't cover last line of the note
				// seehttps://github.com/laurent22/joplin/issues/3607
				paddingBottom={ Platform.OS === 'ios' ? 40 : 0}
			/>
			<View style={{ flexDirection: 'row', flex: 0 }}>
				<TouchableOpacity onPress={_e => wrapSelectionWith('**')}>
					<Text style={{ padding: 8, fontSize: 16, fontWeight: 'bold' }}>
						{'B'}
					</Text>
				</TouchableOpacity>
				<TouchableOpacity onPress={_e => wrapSelectionWith('_')}>
					<Text style={{ padding: 8, fontSize: 16, fontStyle: 'italic' }}>
						{'I'}
					</Text>
				</TouchableOpacity>
			</View>
		</View>
	);

});
