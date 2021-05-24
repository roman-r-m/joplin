import { _ } from '@joplin/lib/locale';
import React = require('react');
const { TextInput } = require('react-native');

interface Props {
    defaultValue: string;
    style: any;
    onChangeText: Function;
    onSelectionChange: Function;
    selectionColor: any;
    keyboardAppearance: any;
    placeholderTextColor: any;
}

interface State {
    defaultValue: string;
}

class NoteBodyEditor extends React.Component<Props, State> {
	// inputRef: React.RefObject<typeof TextInput>;
	constructor(props: Props) {
		super(props);
		// this.inputRef = React.createRef();
		this.state = { defaultValue: props.defaultValue };
	}

	// componentDidMount() {
	//     console.log(`set value ${this.props.defaultValue}`);
	//     this.inputRef.current.value = this.props.defaultValue;
	// }

	render() {
		return (
			<TextInput
				autoCapitalize="sentences"
				style={this.props.style}
				// ref={this.inputRef}
				multiline={true}
				defaultValue={this.state.defaultValue}
				onChangeText={this.props.onChangeText}
				onSelectionChange={this.props.onSelectionChange}
				blurOnSubmit={false}
				selectionColor={this.props.selectionColor}
				keyboardAppearance={this.props.keyboardAppearance}
				placeholder={_('Add body')}
				placeholderTextColor={this.props.placeholderTextColor}
			/>
		);
	}
}

export default NoteBodyEditor;
