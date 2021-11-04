# Vbmeta Patcher

Vbmeta Patcher is an Android app that toggles the disable flags of the vbmeta partitions.

## Usage

If either `vbmeta_a` or `vbmeta_b` are unpatched, pressing the `Patch vbmeta` button will patch the unpatched partitions. If they are both patched, the `Restore vbmeta` button will restore the `vbmeta` partition in the active slot to the unpatched state.

## External Links

[avb_vbmeta_image.h](https://android.googlesource.com/platform/external/avb/+/master/libavb/avb_vbmeta_image.h)

## Credits

Thanks to [RikkaW](https://github.com/RikkaW), as Vbmeta Patcher's design is based heavily on [YASNAC](https://github.com/RikkaW/YASNAC)
