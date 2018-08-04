import shutil

BARREL_NAMES = [
    'barrel',
    'gold_barrel',
    'nether_barrel',
    'diamond_barrel',
    'emerald_barrel',
    'obsidian_barrel',
    'end_stone_barrel',
    'black_hole_barrel'
]

def main():
    state_template = open('scripts/barrel_state_template.json', 'r').read()
    model_template = open('scripts/barrel_model_template.json', 'r').read()

    for barrel_name in BARREL_NAMES:
        state_dest = f'src/main/resources/assets/stockpile/blockstates/{barrel_name}.json'
        model_dest = f'src/main/resources/assets/stockpile/models/{barrel_name}.json'

        with open(state_dest, 'w') as state_fp:
            state_fp.write(state_template.replace('$BARREL_NAME$', barrel_name))

        with open(model_dest, 'w') as model_fp:
            model_fp.write(model_template.replace('$BARREL_NAME$', barrel_name))
        


if __name__ == '__main__':
    main()