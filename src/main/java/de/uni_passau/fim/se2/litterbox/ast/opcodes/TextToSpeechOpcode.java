package de.uni_passau.fim.se2.litterbox.ast.opcodes;

public enum TextToSpeechOpcode {
    text2speech_setVoice, text2speech_speakAndWait, text2speech_setLanguage;

    public static boolean contains(String opcode) {
        for (TextToSpeechOpcode value : TextToSpeechOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }
}
