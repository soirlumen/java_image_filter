package image;

/**
 * enum pro výběr metody výpočtu nového pixelu z barevného na černobílý
 */
public enum ChooseGrayscale {
    /**
     * nejjednodušší, rychlý mean výpočet
     */
    average{
        @Override
        int togray(int r,int g,int b){
            return(r+g+b)/3;
        }
    },
    /**
     * rychlý a vyváženější výpočet
     */
    luminance_bt709{
        @Override
        int togray(int r,int g,int b){
            return (int) (0.2126 * r + 0.7152 * g + 0.0722 * b + 0.5);
        }
    },
    /**
     * výpočet zvýrazňující reakci lidského oka na světlost - zvýrazňuje zelenou
     */
    luma_bt601 {
        @Override
        int togray(int r, int g, int b) {return (int) (0.299 * r + 0.587 * g + 0.114 * b + 0.5);}
    };
    abstract int togray(int r,int g,int b);
}
