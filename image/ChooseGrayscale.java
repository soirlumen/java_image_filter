package image;

public enum ChooseGrayscale {
    average{
        @Override
        int togray(int r,int g,int b){
            return(r+g+b)/3;
        }
    },
    luminance_bt709{
        @Override
        int togray(int r,int g,int b){
            return (int) (0.2126 * r + 0.7152 * g + 0.0722 * b + 0.5);
        }
    },
    luma_bt601 {
        @Override
        int togray(int r, int g, int b) {return (int) (0.299 * r + 0.587 * g + 0.114 * b + 0.5);}
    };
    abstract int togray(int r,int g,int b);
}
