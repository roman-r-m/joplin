"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const React = require("react");
//const { TextInput } = require('react-native');
const react_native_1 = require("react-native");
const MdEditor = react_native_1.requireNativeComponent('MdEditor');
class NoteBodyEditor extends React.Component {
    // inputRef: React.RefObject<typeof TextInput>;
    constructor(props) {
        super(props);
        // this.inputRef = React.createRef();
        this.state = { defaultValue: props.defaultValue };
    }
    // componentDidMount() {
    //     console.log(`set value ${this.props.defaultValue}`);
    //     this.inputRef.current.value = this.props.defaultValue;
    // }
    render() {
        return (React.createElement(MdEditor, null)
        // <TextInput
        // 	enableMdHighlight={true}
        // 	autoCapitalize="sentences"
        // 	style={this.props.style}
        // 	// ref={this.inputRef}
        // 	multiline={true}
        // 	defaultValue={this.state.defaultValue}
        // 	onChangeText={this.props.onChangeText}
        // 	onSelectionChange={this.props.onSelectionChange}
        // 	blurOnSubmit={false}
        // 	selectionColor={this.props.selectionColor}
        // 	keyboardAppearance={this.props.keyboardAppearance}
        // 	placeholder={_('Add body')}
        // 	placeholderTextColor={this.props.placeholderTextColor}
        // />
        );
    }
}
exports.default = NoteBodyEditor;
//# sourceMappingURL=NoteBodyEditor.js.map