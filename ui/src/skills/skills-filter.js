import {bindable, bindingMode, inject, computedFrom, BindingEngine} from 'aurelia-framework';

const concat = (xs,y) => xs.concat(y)
const flatMap = (f,xs) => xs.map(f).reduce(concat, [])

@inject(BindingEngine)
export class SkillsFilterCustomElement {
  @bindable({ defaultBindingMode: bindingMode.twoWay }) skills = [];
  selectedFilters = [];
  allSkills = [];
  tags = [];

  constructor(bindingEngine) {
    this.subscription = bindingEngine.propertyObserver(this, 'skills')
      .subscribe((newValue, oldValue) => this.init(newValue));
  }
  init(skills) {
    if(skills && skills.length) {
      this.allSkills = skills
      this.tags = flatMap(s => s.tags, this.allSkills)
        .filter((value, index, self) => self.indexOf(value) === index)
        .map(t => ({value: t, isActive: false}));
        console.log(this.tags)
      this.subscription.dispose();
    }
  }
  toggleTag(tag) {
    tag.isActive = !tag.isActive;
    this.refresh();
  }
  refresh() {
    let activeTags = this.tags.filter(t => t.isActive);
    this.skills = this.allSkills.filter(s => activeTags.length === 0 || activeTags.some(t => s.tags.some(tg => tg === t.value)));
  }
}
