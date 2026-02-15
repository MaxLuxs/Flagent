// Flagent Design Tokens — generated from tokens.json. Do not edit by hand.

import SwiftUI

public enum FlagentTokens {
    public enum Colors {
        public static let primary = Color(red: 0.054901960784313725, green: 0.6470588235294118, blue: 0.9137254901960784, opacity: 1)
        public static let primaryDark = Color(red: 0.00784313725490196, green: 0.5176470588235295, blue: 0.7803921568627451, opacity: 1)
        public static let primaryLight = Color(red: 0.2196078431372549, green: 0.7411764705882353, blue: 0.9725490196078431, opacity: 1)
        public static let secondary = Color(red: 0.0784313725490196, green: 0.7215686274509804, blue: 0.6509803921568628, opacity: 1)
        public static let secondaryDark = Color(red: 0.050980392156862744, green: 0.5803921568627451, blue: 0.5333333333333333, opacity: 1)
        public static let secondaryLight = Color(red: 0.3686274509803922, green: 0.9176470588235294, blue: 0.8313725490196079, opacity: 1)
        public static let accent = Color(red: 0.9607843137254902, green: 0.6196078431372549, blue: 0.043137254901960784, opacity: 1)
        public static let success = Color(red: 0.06274509803921569, green: 0.7254901960784313, blue: 0.5058823529411764, opacity: 1)
        public static let error = Color(red: 0.9372549019607843, green: 0.26666666666666666, blue: 0.26666666666666666, opacity: 1)
        public static let warning = Color(red: 0.9607843137254902, green: 0.6196078431372549, blue: 0.043137254901960784, opacity: 1)
        public static let info = Color(red: 0.23137254901960785, green: 0.5098039215686274, blue: 0.9647058823529412, opacity: 1)
        public static let neutral = Color(red: 0.4196078431372549, green: 0.4470588235294118, blue: 0.5019607843137255, opacity: 1)
        public static let neutralLight = Color(red: 0.611764705882353, green: 0.6392156862745098, blue: 0.6862745098039216, opacity: 1)
        public static let neutralLighter = Color(red: 0.8196078431372549, green: 0.8352941176470589, blue: 0.8588235294117647, opacity: 1)
        public static let codeBackground = Color(red: 0.11764705882352941, green: 0.1607843137254902, blue: 0.23137254901960785, opacity: 1)
        public static let codeText = Color(red: 0.796078431372549, green: 0.8352941176470589, blue: 0.8823529411764706, opacity: 1)
        public static let overlay = Color(red: 0, green: 0, blue: 0, opacity: 0.5)
        public static let primaryGlow = Color(red: 0.054901960784313725, green: 0.6470588235294118, blue: 0.9137254901960784, opacity: 0.2)
        public enum Dark {
            public static let background = Color(red: 0.058823529411764705, green: 0.09019607843137255, blue: 0.16470588235294117, opacity: 1)
            public static let sidebar_bg = Color(red: 0.11764705882352941, green: 0.1607843137254902, blue: 0.23137254901960785, opacity: 1)
            public static let content_bg = Color(red: 0.058823529411764705, green: 0.09019607843137255, blue: 0.16470588235294117, opacity: 1)
            public static let text = Color(red: 1, green: 1, blue: 1, opacity: 1)
            public static let text_light = Color(red: 1, green: 1, blue: 1, opacity: 0.7)
            public static let border = Color(red: 1, green: 1, blue: 1, opacity: 0.08)
            public static let input_bg = Color(red: 1, green: 1, blue: 1, opacity: 0.06)
            public static let input_border = Color(red: 1, green: 1, blue: 1, opacity: 0.12)
            public static let card_bg = Color(red: 1, green: 1, blue: 1, opacity: 0.04)
            public static let card_border = Color(red: 1, green: 1, blue: 1, opacity: 0.08)
            public static let error_bg = Color(red: 0.9372549019607843, green: 0.26666666666666666, blue: 0.26666666666666666, opacity: 0.2)
            public static let error_text = Color(red: 0.9882352941176471, green: 0.6470588235294118, blue: 0.6470588235294118, opacity: 1)
            public static let badge_bg = Color(red: 1, green: 1, blue: 1, opacity: 0.12)
            public static let success_bg = Color(red: 0.06274509803921569, green: 0.7254901960784313, blue: 0.5058823529411764, opacity: 0.2)
            public static let warning_bg = Color(red: 0.9607843137254902, green: 0.6196078431372549, blue: 0.043137254901960784, opacity: 0.2)
            public static let info_bg = Color(red: 0.23137254901960785, green: 0.5098039215686274, blue: 0.9647058823529412, opacity: 0.2)
            public static let text_muted = Color(red: 1, green: 1, blue: 1, opacity: 0.5)
        }
    }
    public static let shadow = "rgba(0, 0, 0, 0.08)"
    public static let shadowHover = "rgba(0, 0, 0, 0.15)"
    public static let shadowCard = "0 2px 8px var(--flagent-shadow-default)"
    public static let shadowModal = "0 10px 25px var(--flagent-shadow-hover)"
    public static let gradientPrimary = "linear-gradient(135deg, #0EA5E9 0%, #0284C7 100%)"
    public static let gradientSecondary = "linear-gradient(135deg, #14B8A6 0%, #0D9488 100%)"
    public static let gradientHero = "linear-gradient(135deg, #0f172a 0%, #1e293b 20%, #0f172a 40%, #1e3a5f 60%, #0f172a 80%, #1e293b 100%)"
    public enum Spacing {
        public static let _4: CGFloat = 4
        public static let _6: CGFloat = 6
        public static let _8: CGFloat = 8
        public static let _10: CGFloat = 10
        public static let _12: CGFloat = 12
        public static let _16: CGFloat = 16
        public static let _20: CGFloat = 20
        public static let _24: CGFloat = 24
        public static let _25: CGFloat = 25
    }
    public enum Radius {
        public static let sm: CGFloat = 3
        public static let md: CGFloat = 6
        public static let lg: CGFloat = 8
        public static let card: CGFloat = 10
    }
}