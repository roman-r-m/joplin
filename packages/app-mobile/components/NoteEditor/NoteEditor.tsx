const { _ } = require('@joplin/lib/locale');
import Setting from '@joplin/lib/models/Setting';
import { Text, NativeSyntheticEvent, TextInputSelectionChangeEventData, TouchableOpacity, View } from 'react-native';
const { Platform, TextInput } = require('react-native');
const React = require('react');
const { connect } = require('react-redux');

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

interface State {
	text: string;
	selection: Selection;
}

class NoteEditorComponent extends React.Component<Props, State> {

	state: State;

	constructor(props: Props) {
		super(props);

		this.state = {
			text: props.defaultValue,
			selection: props.initialSelection,
		};

		this.onSelectionChange = this.onSelectionChange.bind(this);
		this.onChangeText = this.onChangeText.bind(this);
	}

	// shouldComponentUpdate(nextProps, nextState) {
	// }

	public async insertAtSelection(insert: string) {
		if (this.state.selection) {
			const start = this.state.selection.start;
			const end = this.state.selection.end;
			const newText = this.state.text.substring(0, start) + insert + this.state.text.substring(end);
			await this.setText(newText);
		} else {
			await this.setText(this.state.text + insert);
		}
	}

	async wrapSelectionWith(delimiter: string) {
		if (!this.state.selection) return;

		const start = this.state.selection.start;
		const end = this.state.selection.end;
		if (start == end) {
			await this.setText(this.state.text.substring(0, start) + delimiter + this.state.text.substring(start));
		} else {
			await this.setText(this.state.text.substring(0, start) + delimiter + this.state.text.substring(start, end) + delimiter + this.state.text.substring(end));
		}
	}

	async onSelectionChange(e: NativeSyntheticEvent<TextInputSelectionChangeEventData>) {
		// console.log(`on selection change ${JSON.stringify(e.nativeEvent.selection)}`);
		// console.log(`this=${this}, state=${this.state}`);
		if (this.state.selection && !e.nativeEvent.selection) return;

		this.setState({ selection: e.nativeEvent.selection });
		this.props.onSelectionChange(e);
	}

	async onChangeText(text: string) {
		await this.setText(text);
	}

	async setText(text: string) {
		await this.setState({ text: text });
		await this.props.onChangeText(text);
	}

	render() {
		const style = Object.assign({}, this.props.style, { flex: 1 });

		let buttons = null;
		if (Setting.value('editor.beta')) {
			buttons =
			<View style={{ flexDirection: 'row', flex: 0 }}>
				<TouchableOpacity onPress={_e => this.wrapSelectionWith('**')}>
					<Text style={{ padding: 8, fontSize: 16, fontWeight: 'bold' }}>
						{'B'}
					</Text>
				</TouchableOpacity>
				<TouchableOpacity onPress={_e => this.wrapSelectionWith('_')}>
					<Text style={{ padding: 8, fontSize: 16, fontStyle: 'italic' }}>
						{'I'}
					</Text>
				</TouchableOpacity>
			</View>;
		}

		return (
			<View style={{ display: 'flex', flex: 1, flexDirection: 'column' }}>
				<TextInput
					enableMarkdown={Setting.value('editor.beta')}
					autoCapitalize="sentences"
					style={style}
					multiline={true}
					value={this.state.text}
					onChangeText={this.onChangeText}
					onSelectionChange={this.onSelectionChange}
					blurOnSubmit={false}
					selectionColor={this.props.theme.textSelectionColor}
					keyboardAppearance={this.props.theme.keyboardAppearance}
					placeholder={_('Add body')}
					placeholderTextColor={this.props.theme.colorFaded}
					// need some extra padding for iOS so that the keyboard won't cover last line of the note
					// seehttps://github.com/laurent22/joplin/issues/3607
					paddingBottom={ Platform.OS === 'ios' ? 40 : 0}
				/>
				{buttons}
			</View>
		);
	}

}

export default connect()(NoteEditorComponent);
