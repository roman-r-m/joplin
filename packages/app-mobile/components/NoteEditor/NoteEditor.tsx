const { _ } = require('@joplin/lib/locale');
import { useImperativeHandle, useRef } from 'react';
import { NativeSyntheticEvent, TextInputSelectionChangeEventData } from 'react-native';
const { Platform, TextInput } = require('react-native');
const React = require('react');

interface Props {
    ref: any;
    style: any;
    value: string;
    onChangeText: (text: string)=> void;
    onSelectionChange: (e: NativeSyntheticEvent<TextInputSelectionChangeEventData>)=> void;
    theme: any;
}

export default React.forwardRef((props: Props, ref: any) => {

	const inputRef = useRef(null);

	useImperativeHandle(ref, () => {
		// TODO
	});

	return (
		<TextInput
			ref={inputRef}
			autoCapitalize="sentences"
			style={props.style}
			multiline={true}
			value={props.value}
			onChangeText={props.onChangeText}
			onSelectionChange={props.onSelectionChange}
			blurOnSubmit={false}
			selectionColor={props.theme.textSelectionColor}
			keyboardAppearance={props.theme.keyboardAppearance}
			placeholder={_('Add body')}
			placeholderTextColor={props.theme.colorFaded}
			// need some extra padding for iO S so that the keyboard won't cover last line of the note
			// seehttps://github.com/laurent22/joplin/issues/3607
			paddingBottom={ Platform.OS === 'ios' ? 40 : 0}
		/>
	);

});
